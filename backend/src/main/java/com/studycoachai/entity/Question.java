package com.studycoachai.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "questions")
public class Question {
    public enum SourceType {
        IPA_PAST_EXAM,
        AI_GENERATED,
        USER_CREATED,
        PRIVATE_NOTE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(name = "study_target_id", nullable = false)
    private Long studyTargetId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_target_id", insertable = false, updatable = false)
    private StudyTarget studyTarget;

    @Column(length = 80)
    private String examType;

    @Column(name = "exam_year")
    private Integer year;

    @Column(length = 40)
    private String season;

    @Column(length = 40)
    private String timeCategory;

    @Column(length = 40)
    private String questionNumber;

    @Column(length = 80)
    private String field;

    @Column(length = 40)
    private String difficulty;

    @Column(nullable = false, length = 4000)
    private String questionText;

    @Column(nullable = false, length = 4000)
    private String choicesJson;

    @Column(nullable = false)
    private Integer answerIndex;

    @Column(length = 4000)
    private String explanation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private SourceType sourceType;

    @Column(length = 200)
    private String sourceLabel;

    @Column(length = 1000)
    private String sourceUrl;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    protected Question() {
    }

    public Question(
            Long userId,
            Long studyTargetId,
            String examType,
            Integer year,
            String season,
            String timeCategory,
            String questionNumber,
            String field,
            String difficulty,
            String questionText,
            String choicesJson,
            Integer answerIndex,
            String explanation,
            SourceType sourceType,
            String sourceLabel,
            String sourceUrl
    ) {
        this.userId = userId;
        this.studyTargetId = studyTargetId;
        update(examType, year, season, timeCategory, questionNumber, field, difficulty, questionText, choicesJson, answerIndex, explanation, sourceType, sourceLabel, sourceUrl);
    }

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public void changeStudyTarget(Long studyTargetId) {
        this.studyTargetId = studyTargetId;
    }

    public void update(
            String examType,
            Integer year,
            String season,
            String timeCategory,
            String questionNumber,
            String field,
            String difficulty,
            String questionText,
            String choicesJson,
            Integer answerIndex,
            String explanation,
            SourceType sourceType,
            String sourceLabel,
            String sourceUrl
    ) {
        this.examType = examType;
        this.year = year;
        this.season = season;
        this.timeCategory = timeCategory;
        this.questionNumber = questionNumber;
        this.field = field;
        this.difficulty = difficulty;
        this.questionText = questionText;
        this.choicesJson = choicesJson;
        this.answerIndex = answerIndex;
        this.explanation = explanation;
        this.sourceType = sourceType == null ? SourceType.USER_CREATED : sourceType;
        this.sourceLabel = sourceLabel;
        this.sourceUrl = sourceUrl;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getStudyTargetId() {
        return studyTargetId;
    }

    public StudyTarget getStudyTarget() {
        return studyTarget;
    }

    public String getExamType() {
        return examType;
    }

    public Integer getYear() {
        return year;
    }

    public String getSeason() {
        return season;
    }

    public String getTimeCategory() {
        return timeCategory;
    }

    public String getQuestionNumber() {
        return questionNumber;
    }

    public String getField() {
        return field;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String getQuestionText() {
        return questionText;
    }

    public String getChoicesJson() {
        return choicesJson;
    }

    public Integer getAnswerIndex() {
        return answerIndex;
    }

    public String getExplanation() {
        return explanation;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public String getSourceLabel() {
        return sourceLabel;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
