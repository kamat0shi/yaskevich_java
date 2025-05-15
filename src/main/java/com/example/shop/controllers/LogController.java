package com.example.shop.controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/logs")
public class LogController {

    private static final Path LOGS_DIR = Paths.get("logs").toAbsolutePath().normalize();
    private static final Pattern SAFE_DATE_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
    private static final String LOG_PATH = "logs/app.log";
    private static final DateTimeFormatter 
        LOG_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @GetMapping
    public ResponseEntity<InputStreamResource> getLogs(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {

        File fullLogFile = new File(LOG_PATH);
        if (!fullLogFile.exists()) {
            return ResponseEntity.notFound().build();
        }

        if (!isValidParamCombination(date, from, to)) {
            return ResponseEntity.badRequest().body(null);
        }

        try {
            Path filteredLogPath = getFilteredLogPath(date, from, to);
            if (!filteredLogPath.startsWith(LOGS_DIR)) {
                return ResponseEntity.badRequest().body(null); // path traversal
            }

            LocalDate fromDate = getFromDate(date, from);
            LocalDate toDate = getToDate(date, to);

            writeFilteredLogs(fullLogFile, filteredLogPath, fromDate, toDate);

            InputStreamResource resource = new InputStreamResource(
                    new FileInputStream(filteredLogPath.toFile()));
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=" + filteredLogPath.getFileName())
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private boolean isValidParamCombination(String date, String from, String to) {
        return date != null || (from != null && to != null);
    }

    private Path getFilteredLogPath(String date, String from, String to) {
        if (date != null) {
            validateDate(date);
            return LOGS_DIR.resolve("log-" + date + ".log").normalize();
        }
        validateDate(from);
        validateDate(to);
        return LOGS_DIR.resolve("log-" + from + "_to_" + to + ".log").normalize();
    }

    private void validateDate(String date) {
        if (!SAFE_DATE_PATTERN.matcher(date).matches()) {
            throw new IllegalArgumentException("Invalid date format: " + date);
        }
    }

    private LocalDate getFromDate(String date, String from) {
        return LocalDate.parse(date != null ? date : from, LOG_DATE_FORMAT);
    }

    private LocalDate getToDate(String date, String to) {
        return LocalDate.parse(date != null ? date : to, LOG_DATE_FORMAT);
    }

    private void writeFilteredLogs(
            File source, Path target, LocalDate fromDate, LocalDate toDate) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(source));
            PrintWriter writer = new PrintWriter(target.toFile())) {

            reader.lines()
                    .filter(line -> isLineInRange(line, fromDate, toDate))
                    .forEach(writer::println);
        }
    }

    private boolean isLineInRange(String line, LocalDate fromDate, LocalDate toDate) {
        try {
            String datePart = line.substring(0, 10);
            LocalDate logDate = LocalDate.parse(datePart, LOG_DATE_FORMAT);
            return !logDate.isBefore(fromDate) && !logDate.isAfter(toDate);
        } catch (Exception e) {
            return false;
        }
    }
}