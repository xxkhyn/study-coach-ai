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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "study_target_id", nullable = false)
    private StudyTarget studyTarget;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(length = 80)
    private String fieldName;

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

    public StudyTask(User user, StudyTarget studyTarget, String title, String fieldName, Integer plannedMinutes, LocalDate dueDate, boolean completed) {
        this.user = user;
        this.studyTarget = studyTarget;
        this.title = title;
        this.fieldName = fieldName;
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

    public void update(StudyTarget studyTarget, String title, String fieldName, Integer plannedMinutes, LocalDate dueDate, boolean completed) {
        this.studyTarget = studyTarget;
        this.title = title;
        this.fieldName = fieldName;
        this.plannedMinutes = plannedMinutes;
        this.dueDate = dueDate;
        this.completed = completed;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public StudyTarget getStudyTarget() {
        return studyTarget;
    }

    public String getTitle() {
        return title;
    }

    public String getFieldName() {
        return fieldName;
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
