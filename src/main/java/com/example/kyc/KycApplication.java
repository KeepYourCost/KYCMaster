package com.example.kyc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class KycApplication {

    public static void main(String[] args) {
        SpringApplication.run(KycApplication.class, args);
    }

}
