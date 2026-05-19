package com.askew.service;

import com.askew.dto.InterviewHistoryItem;
import com.askew.dto.InterviewRequest;
import com.askew.dto.InterviewResponse;

import java.util.List;

public interface InterviewService {
    InterviewResponse generate(InterviewRequest request);
    List<InterviewHistoryItem> findAll();
}