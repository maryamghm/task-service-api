package com.example.taskserviceapi.entity;

import com.example.taskserviceapi.dto.CreateTaskRequest;
import com.example.taskserviceapi.dto.UpdateTaskRequest;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "tasks")
@Data
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    private TaskPriority priority;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User owner;

    /**
     * Factory for task creation with defaults.
     */
    public static Task from(CreateTaskRequest request, User owner) {
        Task task = new Task();
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(TaskStatus.TODO);
        task.setPriority(request.priority() != null ? request.priority() : TaskPriority.MEDIUM);
        task.setOwner(owner);
        return task;
    }

    /**
     * Apply a full update request onto this task.
     */
    public void apply(UpdateTaskRequest request) {
        setTitle(request.title());
        setDescription(request.description());
        setStatus(request.status());
        setPriority(request.priority());
    }
}
