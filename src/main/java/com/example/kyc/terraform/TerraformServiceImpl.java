package com.example.kyc.terraform;

import com.example.kyc.handler.CustomException;
import com.example.kyc.handler.StatusCode;
import com.example.kyc.terraform.dto.AwsCredentialDto;
import com.example.kyc.terraform.dto.TerraformApplyDto;
import com.example.kyc.terraform.dto.TerraformDestroyDto;
import com.example.kyc.terraform.entity.Spot;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class TerraformServiceImpl implements TerraformService {
    private final String TERRAFORM_DIR_PATH = System.getProperty("user.dir") + "/terraform";
    private final String TERRAFORM_FILE_PATH = System.getProperty("user.dir") + "/terraform/main.tf";
    private final String TERRAFORM_VARIABLES_PATH = System.getProperty("user.dir") + "/terraform/variables.tf";
    //    private final String TERRAFORM_BACKUP_VARIABLES_PATH = System.getProperty("user.dir") + "/terraform/backup/variables.tf";
    private final String TERRAFORM_OUTPUT = System.getProperty("user.dir") + "/terraform/terraform-output.json";

    private final ObjectMapper mapper;

    @Qualifier("streamWebClient")
    private final WebClient streamWebClient;

    @Override
    public void initTerraform(AwsCredentialDto awsCredentialDto) throws IOException {
        updateAwsCredentials(awsCredentialDto.getAccessKey(), awsCredentialDto.getSecretKey());
    }

    @Override
    public Flux<String> streamApplyTerraform(TerraformApplyDto terraformApplyDto) {
        return Flux.push(sink -> {
            try {
                updateBackupDir(terraformApplyDto.getBackupDir());
                updateSpotCnt(terraformApplyDto.getCnt());
                String cmd = "terraform init && terraform apply -auto-approve && terraform output -json > terraform-output.json";
                Process process = getProcessBuilder(cmd).start();

                // 비동기적으로 프로세스 출력 읽기
                new Thread(() -> {
                    try {
                        readProcessOutput(process, sink);
                        int exitCode = process.waitFor();
                        sink.next("Process finished with exit code: " + exitCode);
                        sink.complete();
                    } catch (Exception e) {
                        sink.error(e);
                    }
                }).start();
            } catch (Exception e) {
                sink.error(e);
            }
        });
    }
    @Override
    public Flux<String> streamDestroyTerraform(TerraformDestroyDto terraformDestroyDto) {
        return Flux.push(sink -> {
            try {
                JsonNode rootNode = mapper.readTree(new File(TERRAFORM_OUTPUT));
                int removeIdx = terraformDestroyDto.getRemoveIdx();
                int currentIdx = terraformDestroyDto.getCurrentIdx();

                String cmd;
                if (removeIdx == -1 && currentIdx == -1) {
                    // 전체 인스턴스 삭제
                    cmd = "terraform destroy -auto-approve";
                } else {
                    // 개별 인스턴스 삭제
                    if (removeIdx >= this.getSpotList().size() || removeIdx < 0) {
                        sink.error(new CustomException(StatusCode.OUT_RANGE));
                        return;
                    }
                    cmd = String.format("terraform destroy -target=aws_instance.kyc_spot_instance[%d] -auto-approve", removeIdx);
                }

                Process process = getProcessBuilder(cmd).start();

                // 비동기적으로 프로세스 출력 읽기
                new Thread(() -> {
                    try {
                        readProcessOutput(process, sink);
                        int exitCode = process.waitFor();

                        if (exitCode == 0 && removeIdx == -1 && currentIdx == -1) {
                            // 모든 인스턴스 삭제 후 JSON 업데이트
                            ((ArrayNode) rootNode.get("instance_ids").get("value")).removeAll();
                            ((ArrayNode) rootNode.get("public_dns").get("value")).removeAll();
                            ((ArrayNode) rootNode.get("public_ips").get("value")).removeAll();
                            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(TERRAFORM_OUTPUT), rootNode);
                        } else if (exitCode == 0) {
                            // 특정 인스턴스 삭제 후 JSON 업데이트
                            ArrayNode instanceIds = (ArrayNode) rootNode.get("instance_ids").get("value");
                            ArrayNode publicDnsList = (ArrayNode) rootNode.get("public_dns").get("value");
                            ArrayNode publicIps = (ArrayNode) rootNode.get("public_ips").get("value");

                            instanceIds.set(removeIdx, String.format("removed_instance / current instance ID: %d", currentIdx));
                            publicDnsList.set(removeIdx, String.format("removed_instance / current instance ID: %d", currentIdx));
                            publicIps.set(removeIdx, String.format("removed_instance / current instance ID: %d", currentIdx));
                            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(TERRAFORM_OUTPUT), rootNode);
                        }

                        sink.next("Process finished with exit code: " + exitCode);
                        sink.complete();
                    } catch (Exception e) {
                        sink.error(e);
                    }
                }).start();

            } catch (Exception e) {
                sink.error(e);
            }
        });
    }


    private void readProcessOutput(Process process, FluxSink<String> sink) throws IOException {
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        String s;
        // stdout 읽기
        while ((s = stdInput.readLine()) != null) {
            sink.next(s);  // 데이터를 읽을 때마다 바로 sink로 전달
        }

        // stderr 읽기
        while ((s = stdError.readLine()) != null) {
            sink.next("ERROR: " + s);  // 에러 메시지도 바로 전달
        }
    }

    @Override
    public int applyTerraform(TerraformApplyDto terraformApplyDto) {
        try {
            updateBackupDir(terraformApplyDto.getBackupDir());
            updateSpotCnt(terraformApplyDto.getCnt());
            String cmd = "terraform init && terraform apply -auto-approve && terraform output -json > terraform-output.json";
            return stdOutReader(getProcessBuilder(cmd).start());
        } catch (Exception e) {
            System.out.println(e);
        }
        return 0;
    }

    @Override
    public void destroyTerraform(TerraformDestroyDto terraformDestroyDto) throws IOException, InterruptedException {
        JsonNode rootNode = mapper.readTree(new File(TERRAFORM_OUTPUT));
        int removeIdx = terraformDestroyDto.getRemoveIdx();
        int currentIdx = terraformDestroyDto.getCurrentIdx();
        if (removeIdx == -1 && currentIdx == -1) {
            String cmd = "terraform destroy -auto-approve";
            int exitCode = stdOutReader(getProcessBuilder(cmd).start());
            ((ArrayNode) rootNode.get("instance_ids").get("value")).removeAll();
            ((ArrayNode) rootNode.get("public_dns").get("value")).removeAll();
            ((ArrayNode) rootNode.get("public_ips").get("value")).removeAll();
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(TERRAFORM_OUTPUT), rootNode);

            System.out.println(exitCode);
        } else {
            if (removeIdx >= this.getSpotList().size() && removeIdx < 0) {
                throw new CustomException(StatusCode.OUT_RANGE);
            }
            String cmd = String.format("terraform destroy -target=aws_instance.kyc_spot_instance[%d] -auto-approve", removeIdx);
            int exitCode = stdOutReader(getProcessBuilder(cmd).start());

            ArrayNode instanceIds = (ArrayNode) rootNode.get("instance_ids").get("value");
            ArrayNode publicDnsList = (ArrayNode) rootNode.get("public_dns").get("value");
            ArrayNode publicIps = (ArrayNode) rootNode.get("public_ips").get("value");

            instanceIds.set(removeIdx, String.format("removed_instance / current instance ID: %d", currentIdx));
            publicDnsList.set(removeIdx, String.format("removed_instance / current instance ID: %d", currentIdx));
            publicIps.set(removeIdx, String.format("removed_instance / current instance ID: %d", currentIdx));
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(TERRAFORM_OUTPUT), rootNode);

            System.out.println(exitCode);
        }
    }

    @Override
    public void shutdown(int removeIdx) throws IOException, InterruptedException {
        int cnt = getSpotList().size();

        if (removeIdx >= cnt && removeIdx < 0) {
            throw new CustomException(StatusCode.OUT_RANGE);
        }
        String cmd = String.format("terraform init && terraform apply -auto-approve -var=\"instance_count=%d\" && terraform output -json > terraform-output.json", cnt + 1);
        int exitCode = stdOutReader(getProcessBuilder(cmd).start());
        if (exitCode == 0) {
            //back up logic 들어갈 예정
            this.destroyTerraform(TerraformDestroyDto.builder()
                    .removeIdx(removeIdx)
                    .currentIdx(cnt).build());
        }
//        String result = streamWebClient.post()
//                .uri("/read/")
//                .body(Mono.just("request"), String.class)
//                .retrieve()
//                .bodyToMono(String.class)
//                .block();

    }

    @Override
    public List<Spot> getSpotList() throws IOException {
        JsonNode jsonNode = mapper.readTree(new File(TERRAFORM_OUTPUT));
        List<Spot> spots = new ArrayList<>();
        List<String> spotIds = mapper.convertValue(jsonNode.get("instance_ids").get("value"), new TypeReference<List<String>>() {
        });
        List<String> publicDnsList = mapper.convertValue(jsonNode.get("public_dns").get("value"), new TypeReference<List<String>>() {
        });
        List<String> publicIps = mapper.convertValue(jsonNode.get("public_ips").get("value"), new TypeReference<List<String>>() {
        });
        for (int i = 0; i < spotIds.size(); i++) {
            spots.add(Spot.builder()
                    .spotIdx(i)
                    .spotId(spotIds.get(i))
                    .publicDns(publicDnsList.get(i))
                    .publicIp(publicIps.get(i))
                    .build());
        }
        return spots;
    }

    private int stdOutReader(Process process) throws InterruptedException, IOException {
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        String s;
        while ((s = stdInput.readLine()) != null) {
            System.out.println(s);
        }

        while ((s = stdError.readLine()) != null) {
            System.err.println(s);
        }

        return process.waitFor();
    }

    private ProcessBuilder getProcessBuilder(String cmd) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("sh", "-c", cmd);
        processBuilder.directory(new File(TERRAFORM_DIR_PATH));
        return processBuilder;
    }

    private String readFile(String _path) throws IOException {
        Path path = Paths.get(_path);
        return new String(Files.readAllBytes(path));
    }

    private void writeTerraformFile(String content, String _path) throws IOException {
        Path path = Paths.get(_path);
        Files.write(path, content.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
    }

    private void updateAwsCredentials(String accessKey, String secretKey) throws IOException {
//        updateBackupAwsCredentials(accessKey, secretKey);
        String content = readFile(TERRAFORM_VARIABLES_PATH);
        String updatedContent = content.replaceAll(
                "(variable \"access_key\"\\s*\\{\\s*description\\s*=\\s*\"AWS account access key\"\\s*type\\s*=\\s*string\\s*default\\s*=\\s*\")[^\"]*(\"\\s*})",
                "$1" + accessKey + "$2"
        ).replaceAll(
                "(variable \"secret_key\"\\s*\\{\\s*description\\s*=\\s*\"AWS account secret key\"\\s*type\\s*=\\s*string\\s*default\\s*=\\s*\")[^\"]*(\"\\s*})",
                "$1" + secretKey + "$2"
        );
        writeTerraformFile(updatedContent, TERRAFORM_VARIABLES_PATH);
    }

//    private void updateBackupAwsCredentials(String accessKey, String secretKey) throws IOException {
//        String content = readFile(TERRAFORM_BACKUP_VARIABLES_PATH);
//        String updatedContent = content.replaceAll(
//                "(variable \"access_key\"\\s*\\{\\s*description\\s*=\\s*\"AWS account access key\"\\s*type\\s*=\\s*string\\s*default\\s*=\\s*\")[^\"]*(\"\\s*})",
//                "$1" + accessKey + "$2"
//        ).replaceAll(
//                "(variable \"secret_key\"\\s*\\{\\s*description\\s*=\\s*\"AWS account secret key\"\\s*type\\s*=\\s*string\\s*default\\s*=\\s*\")[^\"]*(\"\\s*})",
//                "$1" + secretKey + "$2"
//        );
//        writeTerraformFile(updatedContent,TERRAFORM_BACKUP_VARIABLES_PATH);
//    }

    private void updateBackupDir(String backupDir) throws IOException {
        String content = readFile(TERRAFORM_FILE_PATH);
        String updatedContent = content.replaceAll(
                "(backupDir=)[^\\s]*",
                "$1" + backupDir
        );
        writeTerraformFile(updatedContent, TERRAFORM_FILE_PATH);
    }

    private void updateSpotCnt(String cnt) throws IOException {
        String content = readFile(TERRAFORM_FILE_PATH);
        String updatedContent = content.replaceAll(
                "(variable \"instance_count\"\\s*\\{\\s*type\\s*=\\s*number\\s*default\\s*=\\s*)\\d+(\\s*\\})",
                "$1" + cnt + "$2"
        );
        writeTerraformFile(updatedContent, TERRAFORM_FILE_PATH);
    }

}
