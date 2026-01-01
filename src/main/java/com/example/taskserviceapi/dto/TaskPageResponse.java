package com.example.taskserviceapi.dto;

import com.example.taskserviceapi.entity.Task;
import java.util.List;
import org.springframework.data.domain.Page;

public record TaskPageResponse(
        List<TaskResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static TaskPageResponse from(Page<Task> page) {
        List<TaskResponse> content = page.getContent().stream()
                .map(TaskResponse::from)
                .toList();
        return new TaskPageResponse(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
