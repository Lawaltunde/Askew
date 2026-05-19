package com.askew.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;

@ExtendWith(MockitoExtension.class)
class AiServiceTest {

    @Mock
    private ChatClient.Builder builder;

    private ChatClient chatClient;
    private AiServiceImpl aiService;

    @BeforeEach
    void setUp() {
        chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        when(builder.build()).thenReturn(chatClient);
        aiService = new AiServiceImpl(builder);
    }

    @Test
    void generateQuestions_returnsThreeQuestions() {
        when(chatClient.prompt().user(anyString()).call().content())
                .thenReturn("1. Question one\n2. Question two\n3. Question three");

        List<String> questions = aiService.generateQuestions("Software Engineer");

        assertThat(questions).hasSize(3);
    }

    @Test
    void generateQuestions_filtersBlankLines() {
        when(chatClient.prompt().user(anyString()).call().content())
                .thenReturn("1. Question one\n\n2. Question two\n\n3. Question three\n");

        List<String> questions = aiService.generateQuestions("Product Manager");

        assertThat(questions).hasSize(3);
        assertThat(questions).noneMatch(String::isBlank);
    }

    @Test
    void generateQuestions_limitsToThree() {
        when(chatClient.prompt().user(anyString()).call().content())
                .thenReturn("1. Q1\n2. Q2\n3. Q3\n4. Q4\n5. Q5");

        List<String> questions = aiService.generateQuestions("Engineer");

        assertThat(questions).hasSize(3);
    }

    @Test
    void generateQuestions_tripsWhitespaceFromEachLine() {
        when(chatClient.prompt().user(anyString()).call().content())
                .thenReturn("  1. Q1  \n  2. Q2  \n  3. Q3  ");

        List<String> questions = aiService.generateQuestions("Designer");

        assertThat(questions).allMatch(q -> q.equals(q.trim()));
    }
}