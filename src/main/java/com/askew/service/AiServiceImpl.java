package com.askew.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class AiServiceImpl implements AiService {

    private final ChatClient chatClient;

    public AiServiceImpl(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Override
    public List<String> generateQuestions(String jobTitle) {
        String prompt = String.format(
                "Generate exactly 3 thoughtful, role-specific interview questions for a %s. " +
                "Return only the 3 questions, one per line, numbered 1. 2. 3. No extra commentary.",
                jobTitle
        );

        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        return Arrays.stream(response.split("\n"))
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .limit(3)
                .toList();
    }
}
