package com.studycoachai.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studycoachai.ai.AiClient;
import com.studycoachai.dto.AiAdvicePromptData;
import com.studycoachai.dto.AiAdviceResponse;
import com.studycoachai.dto.AiGeneratedQuestionResponse;
import com.studycoachai.dto.AiQuestionGenerationResult;
import com.studycoachai.dto.QuestionGenerationPromptData;
import com.studycoachai.dto.QuestionGenerationRequest;
import com.studycoachai.entity.StudyTarget;
import com.studycoachai.repository.QuestionGenerationLogRepository;
import com.studycoachai.repository.QuestionRepository;
import com.studycoachai.repository.StudyTargetRepository;
import org.junit.jupiter.api.Test;

class QuestionGenerationServiceTest {
    private static final Long USER_ID = 1L;
    private static final Long STUDY_TARGET_ID = 10L;

    private final QuestionRepository questionRepository = mock(QuestionRepository.class);
    private final QuestionGenerationLogRepository logRepository = mock(QuestionGenerationLogRepository.class);
    private final StudyTargetRepository studyTargetRepository = mock(StudyTargetRepository.class);

    @Test
    void rejectsGeneratedQuestionWhenChoicesAreNotFour() {
        QuestionGenerationService service = serviceWith(List.of(new AiGeneratedQuestionResponse(
                "ネットワーク",
                "basic",
                "不完全な選択肢の問題",
                List.of("A", "B", "C"),
                1,
                "解説"
        )));

        assertThatThrownBy(() -> service.generate(USER_ID, request()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must include exactly 4 choices");
    }

    @Test
    void rejectsGeneratedQuestionWhenAnswerIndexIsOutOfRange() {
        QuestionGenerationService service = serviceWith(List.of(new AiGeneratedQuestionResponse(
                "ネットワーク",
                "basic",
                "正解番号が不正な問題",
                List.of("A", "B", "C", "D"),
                4,
                "解説"
        )));

        assertThatThrownBy(() -> service.generate(USER_ID, request()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("invalid answerIndex");
    }

    private QuestionGenerationService serviceWith(List<AiGeneratedQuestionResponse> questions) {
        when(studyTargetRepository.findByIdAndUserId(STUDY_TARGET_ID, USER_ID))
                .thenReturn(Optional.of(new StudyTarget(USER_ID, "応用情報", null, LocalDate.now().plusMonths(3))));
        return new QuestionGenerationService(
                new FakeAiClient(questions),
                questionRepository,
                logRepository,
                studyTargetRepository,
                new ObjectMapper()
        );
    }

    private QuestionGenerationRequest request() {
        return new QuestionGenerationRequest(
                STUDY_TARGET_ID,
                "応用情報技術者試験",
                "ネットワーク",
                "basic",
                1
        );
    }

    private record FakeAiClient(List<AiGeneratedQuestionResponse> questions) implements AiClient {
        @Override
        public AiAdviceResponse generateDailyAdvice(AiAdvicePromptData promptData) {
            throw new UnsupportedOperationException();
        }

        @Override
        public AiQuestionGenerationResult generateQuestions(QuestionGenerationPromptData promptData) {
            return new AiQuestionGenerationResult("prompt", "{\"questions\":[]}", questions);
        }
    }
}
