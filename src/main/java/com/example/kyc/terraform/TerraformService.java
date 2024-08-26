package com.example.kyc.terraform;

import com.example.kyc.terraform.dto.AwsCredentialDto;
import com.example.kyc.terraform.dto.TerraformApplyDto;
import com.example.kyc.terraform.dto.TerraformDestroyDto;
import com.example.kyc.terraform.entity.Spot;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;

public interface TerraformService {
    void initTerraform(AwsCredentialDto awsCredentialDto) throws IOException;
    int applyTerraform(TerraformApplyDto terraformApplyDto);
    Flux<String> streamApplyTerraform(TerraformApplyDto terraformApplyDto);
    void destroyTerraform(TerraformDestroyDto terraformDestroyDto) throws Exception;
    Flux<String> streamDestroyTerraform(TerraformDestroyDto terraformDestroyDto);
    void shutdown(int removeIdx) throws IOException, InterruptedException;
    List<Spot> getSpotList() throws IOException;
}
