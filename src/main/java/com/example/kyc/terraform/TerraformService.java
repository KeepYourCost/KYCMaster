package com.example.kyc.terraform;

import org.springframework.stereotype.Service;

import java.io.IOException;

public interface TerraformService {
    void initTerraform(int newCnt) throws IOException;
    void applyTerraform();
    void destroyTerraform();
}
