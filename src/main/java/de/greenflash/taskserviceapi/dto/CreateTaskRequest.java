package de.greenflash.taskserviceapi.dto;

import de.greenflash.taskserviceapi.entity.TaskPriority;
import de.greenflash.taskserviceapi.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTaskRequest(
        @NotBlank @Size(max = 255) String title,
        @Size(max = 2000) String description,
        TaskPriority priority
) {
}
