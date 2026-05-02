package com.pathshalapro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * PathshalaPro - SaaS School Management System
 * Entry point for the Spring Boot application.
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
@EnableAsync
public class PathshalaPro {

    public static void main(String[] args) {
        SpringApplication.run(PathshalaPro.class, args);
    }
}
