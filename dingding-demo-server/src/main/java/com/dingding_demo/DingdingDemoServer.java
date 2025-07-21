package com.dingding_demo;

import com.dingding.config.EnableDingdingRpcServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDingdingRpcServer
public class DingdingDemoServer {
    public static void main(String[] args) {
        SpringApplication.run(DingdingDemoServer.class, args);
    }
}
