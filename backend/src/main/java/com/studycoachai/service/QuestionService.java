package com.studycoachai.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studycoachai.dto.FieldAccuracyResponse;
import com.studycoachai.dto.QuestionAnswerRequest;
import com.studycoachai.dto.QuestionAnswerResponse;
import com.studycoachai.dto.QuestionAttemptResponse;
import com.studycoachai.dto.QuestionImportResultResponse;
import com.studycoachai.dto.QuestionRequest;
import com.studycoachai.dto.QuestionResponse;
import com.studycoachai.entity.Question;
import com.studycoachai.entity.Question.SourceType;
import com.studycoachai.entity.QuestionAttempt;
import com.studycoachai.entity.StudyTarget;
import com.studycoachai.exception.ResourceNotFoundException;
import com.studycoachai.repository.QuestionAttemptRepository;
import com.studycoachai.repository.QuestionRepository;
import com.studycoachai.repository.StudyTargetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class QuestionService {
    private static final List<String> REQUIRED_CSV_HEADERS = List.of(
            "examType",
            "year",
            "season",
            "timeCategory",
            "questionNumber",
            "field",
            "difficulty",
            "questionText",
            "choiceA",
            "choiceB",
            "choiceC",
            "choiceD",
            "answer",
            "explanation",
            "sourceType",
            "sourceLabel",
            "sourceUrl"
    );

    private final QuestionRepository questionRepository;
    private final QuestionAttemptRepository questionAttemptRepository;
    private final StudyTargetRepository studyTargetRepository;
    private final ObjectMapper objectMapper;

    public QuestionService(
            QuestionRepository questionRepository,
            QuestionAttemptRepository questionAttemptRepository,
            StudyTargetRepository studyTargetRepository,
            ObjectMapper objectMapper
    ) {
        this.questionRepository = questionRepository;
        this.questionAttemptRepository = questionAttemptRepository;
        this.studyTargetRepository = studyTargetRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<QuestionResponse> list(Long userId) {
        return questionRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public QuestionResponse get(Long userId, Long id) {
        return toResponse(findQuestion(userId, id));
    }

    @Transactional(readOnly = true)
    public QuestionResponse random(Long userId) {
        List<Question> questions = new ArrayList<>(questionRepository.findByUserIdOrderByCreatedAtDesc(userId));
        if (questions.isEmpty()) {
            throw new ResourceNotFoundException("Question not found.");
        }
        Collections.shuffle(questions);
        return toResponse(questions.get(0));
    }

    @Transactional
    public QuestionResponse create(Long userId, QuestionRequest request) {
        StudyTarget target = findTarget(userId, request.studyTargetId());
        Question question = newQuestion(userId, target.getId(), request);
        return toResponse(questionRepository.save(question));
    }

    @Transactional
    public QuestionImportResultResponse importCsv(Long userId, Long studyTargetId, MultipartFile file) {
        StudyTarget target = findTarget(userId, studyTargetId);
        List<String> errors = new ArrayList<>();
        Set<String> seenKeysInFile = new HashSet<>();
        int importedCount = 0;
        int skippedCount = 0;

        List<List<String>> rows;
        try {
            rows = readCsv(file);
        } catch (IOException ex) {
            return new QuestionImportResultResponse(0, 0, 1, List.of("CSV file could not be read."));
        }

        if (rows.isEmpty()) {
            return new QuestionImportResultResponse(0, 0, 1, List.of("CSV file is empty."));
        }

        Map<String, Integer> headerIndexes = headerIndexes(rows.get(0));
        List<String> missingHeaders = REQUIRED_CSV_HEADERS.stream()
                .filter(header -> !headerIndexes.containsKey(header))
                .toList();
        if (!missingHeaders.isEmpty()) {
            return new QuestionImportResultResponse(0, 0, 1, List.of("Missing headers: " + String.join(", ", missingHeaders)));
        }

        for (int rowIndex = 1; rowIndex < rows.size(); rowIndex++) {
            int csvLineNumber = rowIndex + 1;
            List<String> row = rows.get(rowIndex);
            if (row.stream().allMatch(value -> value == null || value.isBlank())) {
                continue;
            }

            try {
                QuestionRequest request = toQuestionRequest(target.getId(), headerIndexes, row);
                String duplicateKey = duplicateKey(request);
                if (seenKeysInFile.contains(duplicateKey) || isDuplicate(userId, request)) {
                    skippedCount++;
                    continue;
                }
                questionRepository.save(newQuestion(userId, target.getId(), request));
                seenKeysInFile.add(duplicateKey);
                importedCount++;
            } catch (IllegalArgumentException ex) {
                errors.add("Line " + csvLineNumber + ": " + ex.getMessage());
            }
        }

        return new QuestionImportResultResponse(importedCount, skippedCount, errors.size(), errors);
    }

    @Transactional
    public QuestionResponse update(Long userId, Long id, QuestionRequest request) {
        Question question = findQuestion(userId, id);
        StudyTarget target = findTarget(userId, request.studyTargetId());
        question.changeStudyTarget(target.getId());
        question.update(
                request.examType(),
                request.year(),
                request.season(),
                request.timeCategory(),
                request.questionNumber(),
                request.field(),
                request.difficulty(),
                request.questionText(),
                writeChoices(request.choices()),
                request.answerIndex(),
                request.explanation(),
                request.sourceType(),
                request.sourceLabel(),
                request.sourceUrl()
        );
        return toResponse(question);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        Question question = findQuestion(userId, id);
        questionAttemptRepository.deleteByUserIdAndQuestionId(userId, question.getId());
        questionRepository.delete(question);
    }

    @Transactional
    public QuestionAnswerResponse answer(Long userId, Long id, QuestionAnswerRequest request) {
        Question question = findQuestion(userId, id);
        boolean correct = question.getAnswerIndex().equals(request.selectedIndex());
        QuestionAttempt attempt = questionAttemptRepository.save(new QuestionAttempt(userId, question.getId(), request.selectedIndex(), correct));
        return new QuestionAnswerResponse(
                attempt.getId(),
                question.getId(),
                request.selectedIndex(),
                question.getAnswerIndex(),
                correct,
                question.getExplanation()
        );
    }

    @Transactional(readOnly = true)
    public List<QuestionResponse> wrongQuestions(Long userId) {
        Map<Long, Question> wrongQuestions = new LinkedHashMap<>();
        for (QuestionAttempt attempt : questionAttemptRepository.findByUserIdOrderByAnsweredAtDesc(userId)) {
            if (Boolean.FALSE.equals(attempt.getCorrect()) && attempt.getQuestion() != null) {
                wrongQuestions.putIfAbsent(attempt.getQuestionId(), attempt.getQuestion());
            }
        }
        return wrongQuestions.values().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<QuestionAttemptResponse> attempts(Long userId) {
        return questionAttemptRepository.findByUserIdOrderByAnsweredAtDesc(userId).stream()
                .map(this::toAttemptResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FieldAccuracyResponse> attemptAccuracyByField(Long userId) {
        Map<String, List<QuestionAttempt>> byField = questionAttemptRepository.findByUserIdOrderByAnsweredAtDesc(userId).stream()
                .filter(attempt -> attempt.getQuestion() != null)
                .collect(Collectors.groupingBy(attempt -> normalizeField(attempt.getQuestion().getField())));

        return byField.entrySet().stream()
                .map(entry -> {
                    int solved = entry.getValue().size();
                    int correct = (int) entry.getValue().stream().filter(attempt -> Boolean.TRUE.equals(attempt.getCorrect())).count();
                    BigDecimal rate = solved == 0
                            ? BigDecimal.ZERO
                            : BigDecimal.valueOf(correct).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(solved), 2, RoundingMode.HALF_UP);
                    return new FieldAccuracyResponse(entry.getKey(), solved, correct, rate);
                })
                .sorted(Comparator.comparing(FieldAccuracyResponse::accuracyRate))
                .toList();
    }

    private Question newQuestion(Long userId, Long studyTargetId, QuestionRequest request) {
        return new Question(
                userId,
                studyTargetId,
                request.examType(),
                request.year(),
                request.season(),
                request.timeCategory(),
                request.questionNumber(),
                request.field(),
                request.difficulty(),
                request.questionText(),
                writeChoices(request.choices()),
                request.answerIndex(),
                request.explanation(),
                request.sourceType(),
                request.sourceLabel(),
                request.sourceUrl()
        );
    }

    private boolean isDuplicate(Long userId, QuestionRequest request) {
        return questionRepository.existsByUserIdAndExamTypeAndYearAndSeasonAndTimeCategoryAndQuestionNumber(
                userId,
                request.examType(),
                request.year(),
                request.season(),
                request.timeCategory(),
                request.questionNumber()
        );
    }

    private String duplicateKey(QuestionRequest request) {
        return String.join("\u001F",
                nullToKey(request.examType()),
                request.year() == null ? "" : request.year().toString(),
                nullToKey(request.season()),
                nullToKey(request.timeCategory()),
                nullToKey(request.questionNumber())
        );
    }

    private String nullToKey(String value) {
        return value == null ? "" : value;
    }

    private QuestionRequest toQuestionRequest(Long studyTargetId, Map<String, Integer> headers, List<String> row) {
        List<String> choices = List.of(
                requiredValue(headers, row, "choiceA"),
                requiredValue(headers, row, "choiceB"),
                requiredValue(headers, row, "choiceC"),
                requiredValue(headers, row, "choiceD")
        );
        return new QuestionRequest(
                studyTargetId,
                value(headers, row, "examType"),
                parseYear(value(headers, row, "year")),
                value(headers, row, "season"),
                value(headers, row, "timeCategory"),
                value(headers, row, "questionNumber"),
                value(headers, row, "field"),
                value(headers, row, "difficulty"),
                requiredValue(headers, row, "questionText"),
                choices,
                parseAnswer(requiredValue(headers, row, "answer")),
                value(headers, row, "explanation"),
                parseSourceType(value(headers, row, "sourceType")),
                value(headers, row, "sourceLabel"),
                value(headers, row, "sourceUrl")
        );
    }

    private List<List<String>> readCsv(MultipartFile file) throws IOException {
        List<List<String>> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            StringBuilder currentRecord = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                if (!currentRecord.isEmpty()) {
                    currentRecord.append('\n');
                }
                currentRecord.append(line);
                if (hasClosedQuotes(currentRecord.toString())) {
                    rows.add(parseCsvRecord(currentRecord.toString()));
                    currentRecord.setLength(0);
                }
            }
            if (!currentRecord.isEmpty()) {
                rows.add(parseCsvRecord(currentRecord.toString()));
            }
        }
        return rows;
    }

    private boolean hasClosedQuotes(String record) {
        boolean inQuotes = false;
        for (int i = 0; i < record.length(); i++) {
            char current = record.charAt(i);
            if (current == '"') {
                if (inQuotes && i + 1 < record.length() && record.charAt(i + 1) == '"') {
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            }
        }
        return !inQuotes;
    }

    private List<String> parseCsvRecord(String record) {
        List<String> values = new ArrayList<>();
        StringBuilder currentValue = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < record.length(); i++) {
            char current = record.charAt(i);
            if (current == '"') {
                if (inQuotes && i + 1 < record.length() && record.charAt(i + 1) == '"') {
                    currentValue.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (current == ',' && !inQuotes) {
                values.add(currentValue.toString().trim());
                currentValue.setLength(0);
            } else {
                currentValue.append(current);
            }
        }
        values.add(currentValue.toString().trim());
        return values;
    }

    private Map<String, Integer> headerIndexes(List<String> headers) {
        Map<String, Integer> indexes = new HashMap<>();
        for (int index = 0; index < headers.size(); index++) {
            indexes.put(cleanHeader(headers.get(index)), index);
        }
        return indexes;
    }

    private String cleanHeader(String header) {
        return header == null ? "" : header.replace("\uFEFF", "").trim();
    }

    private String requiredValue(Map<String, Integer> headers, List<String> row, String key) {
        String value = value(headers, row, key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(key + " is required.");
        }
        return value;
    }

    private String value(Map<String, Integer> headers, List<String> row, String key) {
        Integer index = headers.get(key);
        if (index == null || index >= row.size()) {
            return null;
        }
        String value = row.get(index);
        return value == null || value.isBlank() ? null : value.trim();
    }

    private Integer parseYear(String rawValue) {
        if (rawValue == null) {
            return null;
        }
        try {
            int year = Integer.parseInt(rawValue);
            if (year < 1900 || year > 2100) {
                throw new IllegalArgumentException("year must be between 1900 and 2100.");
            }
            return year;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("year must be a number.");
        }
    }

    private Integer parseAnswer(String rawValue) {
        return switch (rawValue.trim()) {
            case "0", "ア" -> 0;
            case "1", "イ" -> 1;
            case "2", "ウ" -> 2;
            case "3", "エ" -> 3;
            default -> throw new IllegalArgumentException("answer must be ア/イ/ウ/エ or 0/1/2/3.");
        };
    }

    private SourceType parseSourceType(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return SourceType.USER_CREATED;
        }
        Set<String> sourceTypes = Set.of("IPA_PAST_EXAM", "AI_GENERATED", "USER_CREATED", "PRIVATE_NOTE");
        String value = rawValue.trim();
        if (!sourceTypes.contains(value)) {
            throw new IllegalArgumentException("sourceType is invalid: " + value);
        }
        return SourceType.valueOf(value);
    }

    private StudyTarget findTarget(Long userId, Long studyTargetId) {
        return studyTargetRepository.findByIdAndUserId(studyTargetId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Study target not found: " + studyTargetId));
    }

    private Question findQuestion(Long userId, Long id) {
        return questionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found: " + id));
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

    private QuestionAttemptResponse toAttemptResponse(QuestionAttempt attempt) {
        Question question = attempt.getQuestion();
        return new QuestionAttemptResponse(
                attempt.getId(),
                attempt.getUserId(),
                attempt.getQuestionId(),
                question == null ? null : question.getQuestionText(),
                question == null || question.getStudyTarget() == null ? null : question.getStudyTarget().getName(),
                question == null ? null : question.getField(),
                attempt.getSelectedIndex(),
                question == null ? null : question.getAnswerIndex(),
                attempt.getCorrect(),
                attempt.getAnsweredAt()
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

    private String normalizeField(String field) {
        return field == null || field.isBlank() ? "未設定" : field;
    }
}
