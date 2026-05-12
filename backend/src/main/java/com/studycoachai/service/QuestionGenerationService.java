package com.studycoachai.service;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studycoachai.ai.AiClient;
import com.studycoachai.dto.AiGeneratedQuestionResponse;
import com.studycoachai.dto.AiQuestionGenerationResult;
import com.studycoachai.dto.QuestionGenerationLogResponse;
import com.studycoachai.dto.QuestionGenerationPromptData;
import com.studycoachai.dto.QuestionGenerationRequest;
import com.studycoachai.dto.QuestionResponse;
import com.studycoachai.entity.Question;
import com.studycoachai.entity.Question.SourceType;
import com.studycoachai.entity.QuestionGenerationLog;
import com.studycoachai.entity.StudyTarget;
import com.studycoachai.exception.ResourceNotFoundException;
import com.studycoachai.repository.QuestionGenerationLogRepository;
import com.studycoachai.repository.QuestionRepository;
import com.studycoachai.repository.StudyTargetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuestionGenerationService {
    private static final Set<String> SUPPORTED_EXAM_TYPES = Set.of("応用情報技術者試験", "証券外務員一種");
    private static final Set<String> SUPPORTED_DIFFICULTIES = Set.of("basic", "standard", "advanced");

    private final AiClient aiClient;
    private final QuestionRepository questionRepository;
    private final QuestionGenerationLogRepository questionGenerationLogRepository;
    private final StudyTargetRepository studyTargetRepository;
    private final ObjectMapper objectMapper;

    public QuestionGenerationService(
            AiClient aiClient,
            QuestionRepository questionRepository,
            QuestionGenerationLogRepository questionGenerationLogRepository,
            StudyTargetRepository studyTargetRepository,
            ObjectMapper objectMapper
    ) {
        this.aiClient = aiClient;
        this.questionRepository = questionRepository;
        this.questionGenerationLogRepository = questionGenerationLogRepository;
        this.studyTargetRepository = studyTargetRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public List<QuestionResponse> generate(Long userId, QuestionGenerationRequest request) {
        validateRequest(request);
        StudyTarget target = findTarget(userId, request.studyTargetId());

        AiQuestionGenerationResult result = aiClient.generateQuestions(new QuestionGenerationPromptData(
                target.getName(),
                request.examType(),
                request.field(),
                request.difficulty(),
                request.count()
        ));

        validateGeneratedQuestions(result.questions(), request.count());
        List<Question> savedQuestions = result.questions().stream()
                .map(generated -> toQuestion(userId, target.getId(), request, generated))
                .map(questionRepository::save)
                .toList();

        questionGenerationLogRepository.save(new QuestionGenerationLog(
                userId,
                target.getId(),
                request.examType(),
                request.field(),
                request.difficulty(),
                request.count(),
                result.prompt(),
                result.rawResponse()
        ));

        return savedQuestions.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<QuestionGenerationLogResponse> history(Long userId) {
        return questionGenerationLogRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(QuestionGenerationLogResponse::from)
                .toList();
    }

    private void validateRequest(QuestionGenerationRequest request) {
        if (!SUPPORTED_EXAM_TYPES.contains(request.examType())) {
            throw new IllegalArgumentException("examType must be 応用情報技術者試験 or 証券外務員一種.");
        }
        if (!SUPPORTED_DIFFICULTIES.contains(request.difficulty())) {
            throw new IllegalArgumentException("difficulty must be basic, standard, or advanced.");
        }
    }

    private void validateGeneratedQuestions(List<AiGeneratedQuestionResponse> questions, int expectedCount) {
        if (questions == null || questions.isEmpty()) {
            throw new IllegalStateException("AI did not return any questions.");
        }
        if (questions.size() != expectedCount) {
            throw new IllegalStateException("AI returned " + questions.size() + " questions, but " + expectedCount + " were requested.");
        }
        for (int index = 0; index < questions.size(); index++) {
            AiGeneratedQuestionResponse question = questions.get(index);
            if (isBlank(question.questionText())) {
                throw new IllegalStateException("AI question " + (index + 1) + " is missing questionText.");
            }
            if (question.choices() == null || question.choices().size() != 4 || question.choices().stream().anyMatch(this::isBlank)) {
                throw new IllegalStateException("AI question " + (index + 1) + " must include exactly 4 choices.");
            }
            if (question.answerIndex() == null || question.answerIndex() < 0 || question.answerIndex() > 3) {
                throw new IllegalStateException("AI question " + (index + 1) + " has an invalid answerIndex.");
            }
            if (isBlank(question.explanation())) {
                throw new IllegalStateException("AI question " + (index + 1) + " is missing explanation.");
            }
        }
    }

    private Question toQuestion(
            Long userId,
            Long studyTargetId,
            QuestionGenerationRequest request,
            AiGeneratedQuestionResponse generated
    ) {
        return new Question(
                userId,
                studyTargetId,
                request.examType(),
                null,
                null,
                null,
                null,
                request.field(),
                request.difficulty(),
                generated.questionText(),
                writeChoices(generated.choices()),
                generated.answerIndex(),
                generated.explanation(),
                SourceType.AI_GENERATED,
                "AI生成問題",
                null
        );
    }

    private StudyTarget findTarget(Long userId, Long studyTargetId) {
        return studyTargetRepository.findByIdAndUserId(studyTargetId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Study target not found: " + studyTargetId));
    }

    private QuestionResponse toResponse(Question question) {
        return new QuestionResponse(
                question.getId(),
                question.getUserId(),
                question.getStudyTargetId(),
                question.getStudyTarget() == null ? null : question.getStudyTarget().getName(),
                question.getExamType(),
                question.getYear(),
                question.getSeason(),
                question.getTimeCategory(),
                question.getQuestionNumber(),
                question.getField(),
                question.getDifficulty(),
                question.getQuestionText(),
                readChoices(question.getChoicesJson()),
                question.getAnswerIndex(),
                question.getExplanation(),
                question.getSourceType(),
                question.getSourceLabel(),
                question.getSourceUrl(),
                question.getCreatedAt(),
                question.getUpdatedAt()
        );
    }

    private String writeChoices(List<String> choices) {
        try {
            return objectMapper.writeValueAsString(choices);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Choices could not be serialized.", ex);
        }
    }

    private List<String> readChoices(String choicesJson) {
        try {
            return objectMapper.readValue(choicesJson, new TypeReference<List<String>>() {
            });
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Saved question choices could not be parsed.", ex);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
