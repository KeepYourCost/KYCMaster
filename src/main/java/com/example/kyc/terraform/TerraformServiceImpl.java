package com.example.kyc.terraform;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Service
@RequiredArgsConstructor
public class TerraformServiceImpl implements TerraformService{
    private final String TERRAFORM_DIR_PATH = System.getProperty("user.dir") + "/terraform";
    private final String TERRAFORM_FILE_PATH =  System.getProperty("user.dir") + "/terraform/main.tf";
    @Override
    public void initTerraform(int newCnt) throws IOException {
        System.out.println(newCnt);
        updateInstanceCount(newCnt);
    }

    @Override
    public void applyTerraform() {
        try {
            String cmd = "terraform init && terraform apply -auto-approve && terraform output -json > terraform-output.json";
            int exitCode = stdOutReader(getProcessBuilder(cmd).start());
            System.out.println("Exit code: " + exitCode);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    @Override
    public void destroyTerraform() {
        try {
            String cmd = "terraform destroy -auto-approve";
            int exitCode = stdOutReader(getProcessBuilder(cmd).start());
            System.out.println(exitCode);

        } catch (Exception e) {
            System.out.println(e);
        }
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

    private String readTerraformFile() throws IOException {
        Path path = Paths.get(TERRAFORM_FILE_PATH);
        return new String(Files.readAllBytes(path));
    }
    private void writeTerraformFile(String content) throws IOException {
        Path path = Paths.get(TERRAFORM_FILE_PATH);
        Files.write(path, content.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
    }
    private void updateInstanceCount(int newCount) throws IOException {
        String content = readTerraformFile();
        String updatedContent = content.replaceAll(
                "(variable \"instance_count\"\\s*\\{\\s*type\\s*=\\s*number\\s*default\\s*=\\s*)\\d+(\\s*})",
                "$1" + newCount + "$2"
        );
        writeTerraformFile(updatedContent);
    }
}
