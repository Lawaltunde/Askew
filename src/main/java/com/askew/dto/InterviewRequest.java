package com.askew.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record InterviewRequest(
        @NotBlank(message = "Job title is required")
        @Size(max = 200, message = "Job title must not exceed 200 characters")
        @Pattern(
                regexp = "^[a-zA-Z][a-zA-Z0-9\\s\\-.,'/&()]{1,198}[a-zA-Z]$",
                message = "Please enter a valid job title (e.g. Software Engineer, Data Scientist)"
        )
        String jobTitle
) {}
