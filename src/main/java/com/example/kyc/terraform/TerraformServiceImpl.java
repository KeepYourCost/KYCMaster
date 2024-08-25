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
    private final String TERRAFORM_FILE_PATH = System.getProperty("user.dir") + "/terraform/main.tf";
    private final String TERRAFORM_VARIABLES_PATH = System.getProperty("user.dir") + "/terraform/variables.tf";
    private final String TERRAFORM_BACKUP_VARIABLES_PATH = System.getProperty("user.dir") + "/terraform/backup/variables.tf";
    @Override
    public void initTerraform(AwsCredentialDto awsCredentialDto) throws IOException {
        updateAwsCredentials(awsCredentialDto.getAccessKey(), awsCredentialDto.getSecretKey());
    }

    @Override
    public int applyTerraform(String backupDir) {
        try {
            System.out.println(backupDir);
            updateBackupDir(backupDir);
            String cmd = "terraform init && terraform apply -auto-approve && terraform output -json > terraform-output.json";
            return stdOutReader(getProcessBuilder(cmd).start());
        } catch (Exception e) {
            System.out.println(e);
        }
        return 0;
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

    @Override
    public void shutdown() {

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

    private String readTerraformFile(String _path) throws IOException {
        Path path = Paths.get(_path);
        return new String(Files.readAllBytes(path));
    }
    private void writeTerraformFile(String content, String _path) throws IOException {
        Path path = Paths.get(_path);
        Files.write(path, content.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
    }

    private void updateAwsCredentials(String accessKey, String secretKey) throws IOException {
        updateBackupAwsCredentials(accessKey, secretKey);
        String content = readTerraformFile(TERRAFORM_VARIABLES_PATH);
        String updatedContent = content.replaceAll(
                "(variable \"access_key\"\\s*\\{\\s*description\\s*=\\s*\"AWS account access key\"\\s*type\\s*=\\s*string\\s*default\\s*=\\s*\")[^\"]*(\"\\s*})",
                "$1" + accessKey + "$2"
        ).replaceAll(
                "(variable \"secret_key\"\\s*\\{\\s*description\\s*=\\s*\"AWS account secret key\"\\s*type\\s*=\\s*string\\s*default\\s*=\\s*\")[^\"]*(\"\\s*})",
                "$1" + secretKey + "$2"
        );
        writeTerraformFile(updatedContent,TERRAFORM_VARIABLES_PATH);
    }

    private void updateBackupAwsCredentials(String accessKey, String secretKey) throws IOException {
        String content = readTerraformFile(TERRAFORM_BACKUP_VARIABLES_PATH);
        String updatedContent = content.replaceAll(
                "(variable \"access_key\"\\s*\\{\\s*description\\s*=\\s*\"AWS account access key\"\\s*type\\s*=\\s*string\\s*default\\s*=\\s*\")[^\"]*(\"\\s*})",
                "$1" + accessKey + "$2"
        ).replaceAll(
                "(variable \"secret_key\"\\s*\\{\\s*description\\s*=\\s*\"AWS account secret key\"\\s*type\\s*=\\s*string\\s*default\\s*=\\s*\")[^\"]*(\"\\s*})",
                "$1" + secretKey + "$2"
        );
        writeTerraformFile(updatedContent,TERRAFORM_BACKUP_VARIABLES_PATH);
    }

    private void updateBackupDir(String backupDir) throws IOException {
        String content = readTerraformFile(TERRAFORM_FILE_PATH);
        String updatedContent = content.replaceAll(
                "(backupDir=)[^\\s]*",
                "$1" + backupDir
        );
        writeTerraformFile(updatedContent, TERRAFORM_FILE_PATH);
    }

}
