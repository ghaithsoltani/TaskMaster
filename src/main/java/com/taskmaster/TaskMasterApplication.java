package com.taskmaster;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * TaskMaster Application Entry Point.
 *
 * @SpringBootApplication is a convenience annotation that combines:
 * - @Configuration: Marks this as a configuration class
 * - @EnableAutoConfiguration: Enables Spring Boot's auto-configuration
 * - @ComponentScan: Scans for components in this package and sub-packages
 */
@SpringBootApplication
public class TaskMasterApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskMasterApplication.class, args);
    }
}