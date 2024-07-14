package com.example.kyc.terraform;

import com.example.kyc.common.dto.Message;
import com.example.kyc.handler.StatusCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/terraform")
public class TerraformController {
    private final TerraformService terraformService;

    @GetMapping("/")
    public ResponseEntity<Message> apply() {
        terraformService.applyTerraform();
        return ResponseEntity.ok(new Message(StatusCode.OK, "hello world"));
    }
    @GetMapping("/destroy")
    public ResponseEntity<Message> destroy() {
        terraformService.destroyTerraform();
        return ResponseEntity.ok(new Message(StatusCode.OK, "hello world"));
    }

    @GetMapping("/info")
    public ResponseEntity<Message> info() throws IOException {
        terraformService.initTerraform(4);
        return ResponseEntity.ok(new Message(StatusCode.OK, "hello world"));
    }
}
