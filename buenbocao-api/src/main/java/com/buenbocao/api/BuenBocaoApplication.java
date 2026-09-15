package com.buenbocao.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@EnableAsync
@EnableScheduling
@SpringBootApplication
public class BuenBocaoApplication {

    public static void main(String[] args) {
        loadEnvFile();
        SpringApplication.run(BuenBocaoApplication.class, args);
    }

    private static void loadEnvFile() {
        Path envPath = Path.of(".env");
        if (!Files.exists(envPath))
            return;

        try {
            Files.readAllLines(envPath).stream()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#") && line.contains("="))
                    .forEach(line -> {
                        int idx = line.indexOf('=');
                        String key = line.substring(0, idx).trim();
                        String value = line.substring(idx + 1).trim();
                        if (System.getProperty(key) == null && System.getenv(key) == null) {
                            System.setProperty(key, value);
                        }
                    });
        } catch (IOException e) {
            System.err.println("Aviso: No se pudo leer .env: " + e.getMessage());
        }
    }
}