package com.example.kyc.terraform.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Spot {
    private int spotIdx;
    private String spotId;
    private String publicDns;
    private String publicIp;
}
