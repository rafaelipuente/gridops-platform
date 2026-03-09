package com.gridops.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.gridops")
@EntityScan("com.gridops")
@EnableJpaRepositories("com.gridops")
public class GridOpsApplication {

    public static void main(String[] args) {
        SpringApplication.run(GridOpsApplication.class, args);
    }
}
