package com.example.taskserviceapi.dto;

import com.example.taskserviceapi.entity.TaskPriority;
import com.example.taskserviceapi.entity.TaskStatus;
import com.example.taskserviceapi.entity.Task;
import java.time.Instant;

public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        Instant createdAt,
        Instant updatedAt
) {
    /**
     * Map a Task entity into an API response.
     */
    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
