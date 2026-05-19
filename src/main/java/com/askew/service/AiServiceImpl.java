package com.askew.service;

import com.askew.exception.AiServiceException;
import com.askew.exception.InvalidJobTitleException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class AiServiceImpl implements AiService {

    private static final String INVALID_MARKER = "INVALID_JOB_TITLE";

    private final ChatClient chatClient;

    public AiServiceImpl(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Override
    public List<String> generateQuestions(String jobTitle) {
        String prompt = String.format(
                "First, determine if '%s' is a real, recognisable job title or professional role. " +
                "If it is NOT a real job title (e.g. random letters, numbers, gibberish), " +
                "respond with exactly: " + INVALID_MARKER + "\n" +
                "If it IS a real job title, generate exactly 3 thoughtful, role-specific interview questions. " +
                "Return only the 3 questions, one per line, numbered 1. 2. 3. No extra commentary.",
                jobTitle
        );

        try {
            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content()
                    .trim();

            if (response.startsWith(INVALID_MARKER)) {
                throw new InvalidJobTitleException(jobTitle);
            }

            return Arrays.stream(response.split("\n"))
                    .map(String::trim)
                    .filter(line -> !line.isBlank())
                    .limit(3)
                    .toList();

        } catch (InvalidJobTitleException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new AiServiceException(
                    "Failed to generate interview questions. Please try again.", ex
            );
        }
    }
}
