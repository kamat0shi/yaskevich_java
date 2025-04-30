package com.example.shop.controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
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

    private static final String LOG_PATH = "logs/app.log";
    private static final DateTimeFormatter LOG_DATE_FORMAT = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @GetMapping
    public ResponseEntity<InputStreamResource> getLogs(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) {
        File fullLogFile = new File(LOG_PATH);

        if (!fullLogFile.exists()) {
            return ResponseEntity.notFound().build();
        }

        if (date == null && (from == null || to == null)) {
            return ResponseEntity.badRequest()
                    .body(null);
        }

        File filteredLog;
        LocalDate fromDate;
        LocalDate toDate;

        if (date != null) {
            try {
                fromDate = toDate = LocalDate.parse(date, LOG_DATE_FORMAT);
                filteredLog = new File("logs/log-" + date + ".log");
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(null);
            }

        } else {
            try {
                fromDate = LocalDate.parse(from, LOG_DATE_FORMAT);
                toDate = LocalDate.parse(to, LOG_DATE_FORMAT);
                filteredLog = new File("logs/log-" + from + "_to_" + to + ".log");
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(null);
            }
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(fullLogFile));
             PrintWriter writer = new PrintWriter(filteredLog)) {

            List<String> filteredLines = reader.lines()
                    .filter(line -> {
                        try {
                            String datePart = line.substring(0, 10); // yyyy-MM-dd
                            LocalDate logDate = LocalDate.parse(datePart, LOG_DATE_FORMAT);
                            return (logDate.isEqual(fromDate) || logDate.isAfter(fromDate)) 
                                && (logDate.isEqual(toDate) || logDate.isBefore(toDate));
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());

            filteredLines.forEach(writer::println);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }

        try {
            InputStreamResource resource = 
                new InputStreamResource(new FileInputStream(filteredLog));

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=" + filteredLog.getName())
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(resource);

        } catch (FileNotFoundException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}