package com.askew.service;

import com.askew.dto.InterviewHistoryItem;
import com.askew.dto.InterviewRequest;
import com.askew.dto.InterviewResponse;
import com.askew.entity.Interview;
import com.askew.repository.InterviewRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class InterviewServiceImpl implements InterviewService {

    private final AiService aiService;
    private final InterviewRepository repository;

    public InterviewServiceImpl(AiService aiService, InterviewRepository repository) {
        this.aiService = aiService;
        this.repository = repository;
    }

    @Override
    public InterviewResponse generate(InterviewRequest request) {
        List<String> questions = aiService.generateQuestions(request.jobTitle());

        Interview interview = Interview.builder()
                .jobTitle(request.jobTitle())
                .questions(String.join("||", questions))
                .build();
        repository.save(interview);

        return new InterviewResponse(request.jobTitle(), questions);
    }

    @Override
    public List<InterviewHistoryItem> findAll() {
        return repository.findAllByOrderByCreatedAtDesc().stream()
                .map(i -> new InterviewHistoryItem(
                        i.getId(),
                        i.getJobTitle(),
                        Arrays.asList(i.getQuestions().split("\\|\\|")),
                        i.getCreatedAt()
                ))
                .toList();
    }
}