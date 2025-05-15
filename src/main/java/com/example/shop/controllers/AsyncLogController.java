package com.example.shop.controllers;

import com.example.shop.services.AsyncLogService;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/async-logs")
public class AsyncLogController {

    private final AsyncLogService service;

    @Autowired
    public AsyncLogController(AsyncLogService service) {
        this.service = service;
    }

    @GetMapping("/generate")
    public ResponseEntity<Map<String, String>> start(
        @RequestParam String from, @RequestParam String to
    ) {
        String id = UUID.randomUUID().toString();
        service.generate(id, LocalDate.parse(from), LocalDate.parse(to));
        return ResponseEntity.accepted().body(Map.of("requestId", id));
    }

    @GetMapping("/status/{id}")
    public ResponseEntity<String> status(@PathVariable String id) {
        String status = service.getStatus(id);
        if ("NOT_FOUND".equals(status)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(status);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<InputStreamResource> download(
            @PathVariable String id) throws IOException {
        Optional<InputStreamResource> res = service.getFile(id);
        return res.map(resource -> ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=log-" + id + ".log")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(resource))
                .orElse(ResponseEntity.notFound().build());
    }
}