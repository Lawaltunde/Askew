package com.askew.controller;

import com.askew.dto.InterviewHistoryItem;
import com.askew.dto.InterviewRequest;
import com.askew.dto.InterviewResponse;
import com.askew.service.InterviewService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/interviews")
public class InterviewController {

    private final InterviewService interviewService;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @PostMapping("/generate")
    public ResponseEntity<InterviewResponse> generate(@Valid @RequestBody InterviewRequest request) {
        return ResponseEntity.ok(interviewService.generate(request));
    }

    @GetMapping
    public ResponseEntity<List<InterviewHistoryItem>> findAll() {
        return ResponseEntity.ok(interviewService.findAll());
    }
}