package be.ucll;

import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        loadDotEnv();
        SpringApplication.run(Application.class, args);
    }

    private static void loadDotEnv() {
        Path cwd = Path.of("").toAbsolutePath().normalize();
        try {
            Files.createDirectories(cwd.resolve("data"));
            Files.createDirectories(cwd.resolve("backend/data"));
        } catch (Exception ignored) { }

        Path[] candidates = { cwd.resolve(".env"), cwd.resolve("../.env"), cwd.resolve("backend/.env") };

        for (Path envFile : candidates) {
            if (Files.isRegularFile(envFile)) {
                try {
                    for (String line : Files.readAllLines(envFile)) {
                        String entry = line.trim();
                        if (entry.isEmpty() || entry.startsWith("#") || !entry.contains("=")) continue;
                        int separator = entry.indexOf('=');
                        String key = entry.substring(0, separator).trim();
                        String value = entry.substring(separator + 1).trim();
                        if (!key.isEmpty() && System.getProperty(key) == null) {
                            System.setProperty(key, value);
                        }
                    }
                    return;
                } catch (Exception ignored) { }
            }
        }
    }
}