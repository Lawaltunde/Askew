package com.askew.exception;

import com.askew.controller.InterviewController;
import com.askew.service.InterviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InterviewController.class)
@TestPropertySource(properties = "app.cors.allowed-origins=http://localhost:5173")
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InterviewService interviewService;

    @Test
    void returns400_whenJobTitleIsBlank() throws Exception {
        mockMvc.perform(post("/api/v1/interviews/generate")
                        .contentType(APPLICATION_JSON)
                        .content("{\"jobTitle\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/api/v1/interviews/generate"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void returns400_whenJobTitleIsNumericOnly() throws Exception {
        mockMvc.perform(post("/api/v1/interviews/generate")
                        .contentType(APPLICATION_JSON)
                        .content("{\"jobTitle\": \"123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Please enter a valid job title (e.g. Software Engineer, Data Scientist)"));
    }

    @Test
    void returns400_whenBodyIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/interviews/generate")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed or missing request body"));
    }

    @Test
    void returns415_whenContentTypeIsNotJson() throws Exception {
        mockMvc.perform(post("/api/v1/interviews/generate")
                        .contentType("text/plain")
                        .content("Engineer"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415))
                .andExpect(jsonPath("$.message").value("Content-Type must be application/json"));
    }

    @Test
    void returns405_whenMethodIsNotAllowed() throws Exception {
        mockMvc.perform(get("/api/v1/interviews/generate"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.message").value("GET is not supported on this endpoint"));
    }

    @Test
    void returns502_whenAiServiceFails() throws Exception {
        when(interviewService.generate(any()))
                .thenThrow(new AiServiceException(
                        "Failed to generate interview questions. Please try again.",
                        new RuntimeException()
                ));

        mockMvc.perform(post("/api/v1/interviews/generate")
                        .contentType(APPLICATION_JSON)
                        .content("{\"jobTitle\": \"Engineer\"}"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.error").value("Bad Gateway"))
                .andExpect(jsonPath("$.message").value("Failed to generate interview questions. Please try again."));
    }

    @Test
    void returns503_whenDatabaseIsUnavailable() throws Exception {
        when(interviewService.findAll())
                .thenThrow(new DataAccessResourceFailureException("DB unreachable"));

        mockMvc.perform(get("/api/v1/interviews"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.message").value(
                        "The service is temporarily unavailable. Please try again shortly."));
    }

    @Test
    void returns500_whenUnexpectedErrorOccurs() throws Exception {
        when(interviewService.generate(any()))
                .thenThrow(new RuntimeException("Unexpected failure"));

        mockMvc.perform(post("/api/v1/interviews/generate")
                        .contentType(APPLICATION_JSON)
                        .content("{\"jobTitle\": \"Engineer\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }
}
