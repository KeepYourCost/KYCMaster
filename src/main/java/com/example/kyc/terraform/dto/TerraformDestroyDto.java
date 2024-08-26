package com.example.kyc.terraform.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TerraformDestroyDto {
    private int removeIdx;
    private int currentIdx;
}
