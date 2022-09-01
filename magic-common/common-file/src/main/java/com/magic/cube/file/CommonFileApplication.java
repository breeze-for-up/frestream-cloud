package com.magic.cube.file;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @description:
 * 
 * @author: TJ
 * @date:  2022-09-01
 **/
@SpringBootApplication
@EnableDiscoveryClient
public class CommonFileApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommonFileApplication.class, args);
    }
}
