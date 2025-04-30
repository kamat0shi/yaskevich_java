package com.example.shop.controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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

        if (date == null && (from == null || to == null)) {
            return ResponseEntity.badRequest().body(null);
        }

        Path filteredLogPath;
        LocalDate fromDate;
        LocalDate toDate;

        try {
            if (date != null) {
                if (!SAFE_DATE_PATTERN.matcher(date).matches()) {
                    return ResponseEntity.badRequest().body(null);
                }
                fromDate = toDate = LocalDate.parse(date, LOG_DATE_FORMAT);
                filteredLogPath = LOGS_DIR.resolve("log-" + date + ".log").normalize();
            } else {
                if (!SAFE_DATE_PATTERN.matcher(from)    
                    .matches() || !SAFE_DATE_PATTERN.matcher(to).matches()) {
                    return ResponseEntity.badRequest().body(null);
                }
                fromDate = LocalDate.parse(from, LOG_DATE_FORMAT);
                toDate = LocalDate.parse(to, LOG_DATE_FORMAT);
                filteredLogPath = 
                    LOGS_DIR.resolve("log-" + from + "_to_" + to + ".log").normalize();
            }

            if (!filteredLogPath.startsWith(LOGS_DIR)) {
                return ResponseEntity.badRequest().body(null); // path traversal detected
            }

            // Filter log lines by date
            try (BufferedReader reader = new BufferedReader(new FileReader(fullLogFile));
                 PrintWriter writer = new PrintWriter(filteredLogPath.toFile())) {

                List<String> filteredLines = reader.lines()
                        .filter(line -> {
                            try {
                                String datePart = line.substring(0, 10);
                                LocalDate logDate = LocalDate.parse(datePart, LOG_DATE_FORMAT);
                                return (logDate.isEqual(fromDate) || logDate.isAfter(fromDate)) 
                                    &&
                                       (logDate.isEqual(toDate) || logDate.isBefore(toDate));
                            } catch (Exception e) {
                                return false;
                            }
                        }).collect(Collectors.toList());

                filteredLines.forEach(writer::println);
            }

            InputStreamResource resource = 
                new InputStreamResource(new FileInputStream(filteredLogPath.toFile()));
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=" + filteredLogPath.getFileName().toString())
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}