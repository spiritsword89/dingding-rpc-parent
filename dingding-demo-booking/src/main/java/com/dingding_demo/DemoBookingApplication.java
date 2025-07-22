package com.dingding_demo;

import com.dingding.config.EnableDingdingRpcClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
@EnableDingdingRpcClient(clientId = "demo-booking", packages = {"com.dingding_demo.booking.service"})
public class DemoBookingApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoBookingApplication.class, args);
    }
}
