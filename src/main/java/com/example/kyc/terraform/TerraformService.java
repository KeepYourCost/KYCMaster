package com.example.kyc.terraform;

import org.springframework.stereotype.Service;

import java.io.IOException;

public interface TerraformService {
    void initTerraform(AwsCredentialDto awsCredentialDto) throws IOException;
    int applyTerraform(String backupDir);
    void destroyTerraform();
    void shutdown();
}
