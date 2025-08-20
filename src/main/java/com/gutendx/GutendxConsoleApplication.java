package com.gutendx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GutendxConsoleApplication {

    public static void main(String[] args) {
        // Configurar para que no inicie servidor web
        System.setProperty("spring.main.web-application-type", "none");

        SpringApplication.run(GutendxConsoleApplication.class, args);
    }
}