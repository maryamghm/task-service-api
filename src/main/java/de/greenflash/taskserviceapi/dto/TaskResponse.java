package de.greenflash.taskserviceapi.dto;

import de.greenflash.taskserviceapi.entity.TaskPriority;
import de.greenflash.taskserviceapi.entity.TaskStatus;
import de.greenflash.taskserviceapi.entity.Task;
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
