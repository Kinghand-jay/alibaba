package com.jay.alibaba.nacos90;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class NacosProvider90 {

    public static void main(String[] args) {
        SpringApplication.run(NacosProvider90.class);
    }
}
