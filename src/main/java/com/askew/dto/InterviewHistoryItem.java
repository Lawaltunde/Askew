package com.askew.dto;

import java.time.LocalDateTime;
import java.util.List;

public record InterviewHistoryItem(
        Long id,
        String jobTitle,
        List<String> questions,
        LocalDateTime createdAt
) {}
