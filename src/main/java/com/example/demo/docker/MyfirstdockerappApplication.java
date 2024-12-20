package com.example.demo.docker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@RequestMapping("/api")  // Sets a base path for all endpoints
public class MyfirstdockerappApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyfirstdockerappApplication.class, args);
    }

   
    // New welcome message endpoint
    @GetMapping("/msg/welcome")
    public String getWelcomeMessage() {
        return "Hello welcome to my web Application...!";
    }

    // Docker-specific message
    @GetMapping("/msg/docker")
    public String getMsgDocker() {
        return "Hello from Docker!";
    }

    // Kubernetes-specific message
    @GetMapping("/msg/kubernetes")
    public String getKubernetesMessage() {
        return "Hello from Kubernetes!";
    }
}
