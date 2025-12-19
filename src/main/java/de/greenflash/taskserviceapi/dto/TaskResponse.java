package de.greenflash.taskserviceapi.dto;

import de.greenflash.taskserviceapi.entity.TaskPriority;
import de.greenflash.taskserviceapi.entity.TaskStatus;
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
}
