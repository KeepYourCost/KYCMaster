package com.example.kyc.terraform;

import com.example.kyc.common.dto.Message;
import com.example.kyc.handler.StatusCode;
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
    public ResponseEntity<Message> apply(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(new Message(StatusCode.OK, terraformService.applyTerraform(request.get("backupDir"))));
    }
    @PostMapping("/initialization")
    public ResponseEntity<Message> initialization(@RequestBody AwsCredentialDto awsCredentialDto) throws IOException {
        terraformService.initTerraform(awsCredentialDto);
        return ResponseEntity.ok(new Message(StatusCode.OK));
    }
    @GetMapping("/destroy")
    public ResponseEntity<Message> destroy() {
        terraformService.destroyTerraform();
        return ResponseEntity.ok(new Message(StatusCode.OK));
    }

    @GetMapping("/shutdown")
    public ResponseEntity<Message> shutdownEventListener() {
        terraformService.shutdown();
        return ResponseEntity.ok(new Message(StatusCode.OK));
    }
}
