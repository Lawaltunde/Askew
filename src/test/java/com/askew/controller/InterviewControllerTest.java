package com.askew.controller;

import com.askew.dto.InterviewHistoryItem;
import com.askew.dto.InterviewRequest;
import com.askew.dto.InterviewResponse;
import com.askew.service.InterviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InterviewController.class)
@TestPropertySource(properties = "app.cors.allowed-origins=http://localhost:5173")
class InterviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InterviewService interviewService; // interface → JDK proxy

    private static final List<String> QUESTIONS = List.of(
            "1. Question one", "2. Question two", "3. Question three"
    );

    @Test
    void generate_returns200WithQuestions() throws Exception {
        when(interviewService.generate(any(InterviewRequest.class)))
                .thenReturn(new InterviewResponse("Software Engineer", QUESTIONS));

        mockMvc.perform(post("/api/v1/interviews/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jobTitle\": \"Software Engineer\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobTitle").value("Software Engineer"))
                .andExpect(jsonPath("$.questions.length()").value(3));
    }

    @Test
    void generate_returns400ForBlankJobTitle() throws Exception {
        mockMvc.perform(post("/api/v1/interviews/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jobTitle\": \"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generate_returns400ForMissingJobTitle() throws Exception {
        mockMvc.perform(post("/api/v1/interviews/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generate_returns400ForJobTitleExceeding200Characters() throws Exception {
        String longTitle = "a".repeat(201);

        mockMvc.perform(post("/api/v1/interviews/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jobTitle\": \"" + longTitle + "\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generate_returns400ForWhitespaceOnlyJobTitle() throws Exception {
        mockMvc.perform(post("/api/v1/interviews/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jobTitle\": \"   \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generate_returns400ForNumericOnlyInput() throws Exception {
        mockMvc.perform(post("/api/v1/interviews/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jobTitle\": \"123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Please enter a valid job title (e.g. Software Engineer, Data Scientist)"));
    }

    @Test
    void generate_returns400ForSpecialCharactersOnly() throws Exception {
        mockMvc.perform(post("/api/v1/interviews/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jobTitle\": \"!!!\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generate_returns400ForInputEndingWithNumber() throws Exception {
        mockMvc.perform(post("/api/v1/interviews/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jobTitle\": \"Engineer1\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generate_responseContainsCorrectQuestions() throws Exception {
        when(interviewService.generate(any(InterviewRequest.class)))
                .thenReturn(new InterviewResponse("DevOps Engineer", QUESTIONS));

        mockMvc.perform(post("/api/v1/interviews/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jobTitle\": \"DevOps Engineer\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questions[0]").value("1. Question one"))
                .andExpect(jsonPath("$.questions[1]").value("2. Question two"))
                .andExpect(jsonPath("$.questions[2]").value("3. Question three"));
    }

    @Test
    void findAll_returns200WithHistoryList() throws Exception {
        List<InterviewHistoryItem> history = List.of(
                new InterviewHistoryItem(1L, "Software Engineer", QUESTIONS, LocalDateTime.now()),
                new InterviewHistoryItem(2L, "Product Manager", QUESTIONS, LocalDateTime.now().minusDays(1))
        );
        when(interviewService.findAll()).thenReturn(history);

        mockMvc.perform(get("/api/v1/interviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].jobTitle").value("Software Engineer"))
                .andExpect(jsonPath("$[1].jobTitle").value("Product Manager"));
    }

    @Test
    void findAll_returns200WithEmptyList() throws Exception {
        when(interviewService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/interviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}