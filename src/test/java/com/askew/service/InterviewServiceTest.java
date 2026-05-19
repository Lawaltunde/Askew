package com.askew.service;

import com.askew.dto.InterviewHistoryItem;
import com.askew.dto.InterviewRequest;
import com.askew.dto.InterviewResponse;
import com.askew.entity.Interview;
import com.askew.repository.InterviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InterviewServiceTest {

    @Mock
    private AiService aiService;          // interface → JDK proxy, no byte manipulation needed

    @Mock
    private InterviewRepository repository; // interface → JDK proxy

    @InjectMocks
    private InterviewServiceImpl interviewService; // concrete impl under test

    private static final List<String> QUESTIONS = List.of(
            "1. Question one", "2. Question two", "3. Question three"
    );

    @Test
    void generate_returnsCorrectJobTitle() {
        when(aiService.generateQuestions("Product Manager")).thenReturn(QUESTIONS);
        when(repository.save(any(Interview.class))).thenAnswer(i -> i.getArgument(0));

        InterviewResponse response = interviewService.generate(new InterviewRequest("Product Manager"));

        assertThat(response.jobTitle()).isEqualTo("Product Manager");
    }

    @Test
    void generate_returnsQuestionsFromAiService() {
        when(aiService.generateQuestions("Data Scientist")).thenReturn(QUESTIONS);
        when(repository.save(any(Interview.class))).thenAnswer(i -> i.getArgument(0));

        InterviewResponse response = interviewService.generate(new InterviewRequest("Data Scientist"));

        assertThat(response.questions()).containsExactlyElementsOf(QUESTIONS);
    }

    @Test
    void generate_persistsInterviewWithCorrectJobTitle() {
        when(aiService.generateQuestions("Designer")).thenReturn(QUESTIONS);
        ArgumentCaptor<Interview> captor = ArgumentCaptor.forClass(Interview.class);
        when(repository.save(captor.capture())).thenAnswer(i -> i.getArgument(0));

        interviewService.generate(new InterviewRequest("Designer"));

        assertThat(captor.getValue().getJobTitle()).isEqualTo("Designer");
    }

    @Test
    void generate_storesQuestionsAsDelimitedString() {
        when(aiService.generateQuestions("Engineer")).thenReturn(QUESTIONS);
        ArgumentCaptor<Interview> captor = ArgumentCaptor.forClass(Interview.class);
        when(repository.save(captor.capture())).thenAnswer(i -> i.getArgument(0));

        interviewService.generate(new InterviewRequest("Engineer"));

        assertThat(captor.getValue().getQuestions())
                .isEqualTo("1. Question one||2. Question two||3. Question three");
    }

    @Test
    void generate_alwaysCallsRepository() {
        when(aiService.generateQuestions(any())).thenReturn(QUESTIONS);
        when(repository.save(any(Interview.class))).thenAnswer(i -> i.getArgument(0));

        interviewService.generate(new InterviewRequest("Any Role"));

        verify(repository).save(any(Interview.class));
    }

    @Test
    void generate_delegatesToAiServiceWithCorrectJobTitle() {
        when(aiService.generateQuestions("QA Engineer")).thenReturn(QUESTIONS);
        when(repository.save(any(Interview.class))).thenAnswer(i -> i.getArgument(0));

        interviewService.generate(new InterviewRequest("QA Engineer"));

        verify(aiService).generateQuestions("QA Engineer");
    }

    @Test
    void findAll_returnsItemsOrderedByRepository() {
        Interview i1 = Interview.builder()
                .id(1L).jobTitle("Engineer")
                .questions("Q1||Q2||Q3")
                .createdAt(LocalDateTime.now())
                .build();
        Interview i2 = Interview.builder()
                .id(2L).jobTitle("Designer")
                .questions("Q4||Q5||Q6")
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        when(repository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(i1, i2));

        List<InterviewHistoryItem> result = interviewService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).jobTitle()).isEqualTo("Engineer");
        assertThat(result.get(1).jobTitle()).isEqualTo("Designer");
    }

    @Test
    void findAll_splitsQuestionsFromDelimitedString() {
        Interview interview = Interview.builder()
                .id(1L).jobTitle("PM")
                .questions("Question A||Question B||Question C")
                .createdAt(LocalDateTime.now())
                .build();

        when(repository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(interview));

        List<InterviewHistoryItem> result = interviewService.findAll();

        assertThat(result.get(0).questions())
                .containsExactly("Question A", "Question B", "Question C");
    }

    @Test
    void findAll_returnsEmptyListWhenNoInterviews() {
        when(repository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of());

        List<InterviewHistoryItem> result = interviewService.findAll();

        assertThat(result).isEmpty();
    }
}