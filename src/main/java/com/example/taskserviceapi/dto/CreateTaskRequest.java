package com.example.taskserviceapi.dto;

import com.example.taskserviceapi.entity.TaskPriority;
import com.example.taskserviceapi.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTaskRequest(
        @NotBlank @Size(max = 255) String title,
        @Size(max = 2000) String description,
        TaskPriority priority
) {
}
