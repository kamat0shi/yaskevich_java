package com.example.shop.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.core.io.InputStreamResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncLogService {

    private static final Path LOGS_DIR = Paths.get("logs").toAbsolutePath().normalize();
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final Map<String, String> statusMap = new ConcurrentHashMap<>();
    private final Map<String, Path> fileMap = new ConcurrentHashMap<>();

    @Async
    public void generate(String id, LocalDate from, LocalDate to) {
        statusMap.put(id, "IN_PROGRESS");
        try {
            Thread.sleep(15000);
            Path outPath = LOGS_DIR.resolve("log-" + id + ".log");
            Path inPath = LOGS_DIR.resolve("app.log");
            try (BufferedReader reader = Files.newBufferedReader(inPath);
                PrintWriter writer = new PrintWriter(outPath.toFile())) {

                reader.lines()
                    .filter(line -> {
                        try {
                            String datePart = line.substring(0, 10);
                            LocalDate d = LocalDate.parse(datePart, FORMAT);
                            return !d.isBefore(from) && !d.isAfter(to);
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .forEach(writer::println);
            }
            fileMap.put(id, outPath);
            statusMap.put(id, "DONE");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            statusMap.put(id, "ERROR");
        } catch (Exception e) {
            statusMap.put(id, "ERROR");
        }
    }

    public String getStatus(String id) {
        return statusMap.getOrDefault(id, "NOT_FOUND");
    }

    public Optional<InputStreamResource> getFile(String id) throws IOException {
        Path file = fileMap.get(id);
        if (file != null && Files.exists(file)) {
            return Optional.of(new InputStreamResource(Files.newInputStream(file)));
        }
        return Optional.empty();
    }
}