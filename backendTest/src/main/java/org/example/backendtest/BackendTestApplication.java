package org.example.backendtest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BackendTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendTestApplication.class, args);
    }

}
