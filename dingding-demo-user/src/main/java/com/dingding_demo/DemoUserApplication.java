package com.dingding_demo;

import com.dingding.config.EnableDingdingRpcClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDingdingRpcClient(clientId = "demo-user", packages = "com.dingding_demo.user.service")
public class DemoUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoUserApplication.class, args);
    }
}
