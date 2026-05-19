package com.askew.dto;

import java.util.List;

public record InterviewResponse(String jobTitle, List<String> questions) {}