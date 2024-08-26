package com.example.kyc.terraform.dto;

import lombok.Data;
import lombok.Getter;

@Data
public class TerraformApplyDto {
    private String backupDir;
    private String cnt;
}
