package com.askew.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InterviewRequest(
        @NotBlank(message = "Job title is required")
        @Size(max = 200, message = "Job title must not exceed 200 characters")
        String jobTitle
) {}