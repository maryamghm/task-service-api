package com.example.taskserviceapi.dto;

import com.example.taskserviceapi.entity.Task;
import org.springframework.data.domain.Page;

import java.util.List;

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
