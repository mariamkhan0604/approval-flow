package com.approvalflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Approval Flow application.
 *
 * @SpringBootApplication is a convenience annotation that combines:
 *   - @Configuration       → marks this class as a source of bean definitions
 *   - @EnableAutoConfiguration → tells Spring Boot to auto-configure based on the JARs on the classpath
 *   - @ComponentScan       → scans this package and sub-packages for Spring components
 */
@SpringBootApplication
public class ApprovalFlowApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApprovalFlowApplication.class, args);
    }
}
