package com.studycoachai.entity;

import java.time.LocalDate;
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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "study_tasks")
public class StudyTask {
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

    @Column(nullable = false, length = 160)
    private String title;

    @Column(length = 80)
    private String field;

    private Integer plannedMinutes;

    private LocalDate dueDate;

    @Column(nullable = false)
    private boolean completed;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    protected StudyTask() {
    }

    public StudyTask(Long userId, Long studyTargetId, String title, String field, Integer plannedMinutes, LocalDate dueDate, boolean completed) {
        this.userId = userId;
        this.studyTargetId = studyTargetId;
        this.title = title;
        this.field = field;
        this.plannedMinutes = plannedMinutes;
        this.dueDate = dueDate;
        this.completed = completed;
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

    public void update(Long studyTargetId, String title, String field, Integer plannedMinutes, LocalDate dueDate, boolean completed) {
        this.studyTargetId = studyTargetId;
        this.title = title;
        this.field = field;
        this.plannedMinutes = plannedMinutes;
        this.dueDate = dueDate;
        this.completed = completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
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

    public String getTitle() {
        return title;
    }

    public String getField() {
        return field;
    }

    public Integer getPlannedMinutes() {
        return plannedMinutes;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public boolean isCompleted() {
        return completed;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
