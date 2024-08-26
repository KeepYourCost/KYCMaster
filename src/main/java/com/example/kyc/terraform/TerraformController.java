package com.example.kyc.terraform;

import com.example.kyc.common.dto.Message;
import com.example.kyc.handler.StatusCode;
import com.example.kyc.terraform.dto.AwsCredentialDto;
import com.example.kyc.terraform.dto.TerraformApplyDto;
import com.example.kyc.terraform.dto.TerraformDestroyDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/terraform")
public class TerraformController {
    private final TerraformService terraformService;

    @PostMapping("/")
    public ResponseEntity<Message> apply(@RequestBody TerraformApplyDto terraformApplyDto) {
        return ResponseEntity.ok(new Message(StatusCode.OK, terraformService.applyTerraform(terraformApplyDto)));
    }
    @PostMapping("/initialization")
    public ResponseEntity<Message> initialization(@RequestBody AwsCredentialDto awsCredentialDto) throws IOException {
        terraformService.initTerraform(awsCredentialDto);
        return ResponseEntity.ok(new Message(StatusCode.OK));
    }
    @PostMapping("/destroy")
    public ResponseEntity<Message> destroy(@RequestBody TerraformDestroyDto terraformDestroyDto) throws Exception {
        terraformService.destroyTerraform(terraformDestroyDto);
        return ResponseEntity.ok(new Message(StatusCode.OK));
    }
    @PostMapping("/shutdown")
    public ResponseEntity<Message> shutdownEventListener(@RequestBody Map<String,Integer> backIpIdxMap) throws IOException, InterruptedException {
        terraformService.shutdown(backIpIdxMap.get("removeIdx"));
        return ResponseEntity.ok(new Message(StatusCode.OK));
    }

    @GetMapping("/spot-info")
    public ResponseEntity<Message> spotListUp() throws IOException {
        return ResponseEntity.ok(new Message(StatusCode.OK,terraformService.getSpotList()));
    }

}
