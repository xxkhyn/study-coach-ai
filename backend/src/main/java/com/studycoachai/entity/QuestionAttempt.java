package com.studycoachai.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "question_attempts")
public class QuestionAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", insertable = false, updatable = false)
    private Question question;

    @Column(nullable = false)
    private Integer selectedIndex;

    @Column(nullable = false)
    private Boolean correct;

    @Column(nullable = false)
    private OffsetDateTime answeredAt;

    protected QuestionAttempt() {
    }

    public QuestionAttempt(Long userId, Long questionId, Integer selectedIndex, Boolean correct) {
        this.userId = userId;
        this.questionId = questionId;
        this.selectedIndex = selectedIndex;
        this.correct = correct;
    }

    @PrePersist
    void onCreate() {
        answeredAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public Question getQuestion() {
        return question;
    }

    public Integer getSelectedIndex() {
        return selectedIndex;
    }

    public Boolean getCorrect() {
        return correct;
    }

    public OffsetDateTime getAnsweredAt() {
        return answeredAt;
    }
}
