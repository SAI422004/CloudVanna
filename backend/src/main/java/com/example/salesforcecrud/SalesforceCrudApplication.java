package com.example.salesforcecrud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

@SpringBootApplication
public class SalesforceCrudApplication {

    public static void main(String[] args) {
        loadDotEnv();
        SpringApplication.run(SalesforceCrudApplication.class, args);
    }

    private static void loadDotEnv() {
        String[] paths = {"../.env", ".env", "../../.env"};
        for (String path : paths) {
            File file = new File(path);
            if (file.exists() && file.isFile()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.isEmpty() || line.startsWith("#") || !line.contains("=")) {
                            continue;
                        }
                        int eqIdx = line.indexOf('=');
                        String key = line.substring(0, eqIdx).trim();
                        String val = line.substring(eqIdx + 1).trim();
                        if ((val.startsWith("\"") && val.endsWith("\"")) || (val.startsWith("'") && val.endsWith("'"))) {
                            val = val.substring(1, val.length() - 1);
                        }
                        System.setProperty(key, val);
                    }
                    System.out.println("Loaded environment variables from: " + file.getAbsolutePath());
                    break;
                } catch (IOException e) {
                    System.err.println("Failed to read " + path + ": " + e.getMessage());
                }
            }
        }
    }
}

