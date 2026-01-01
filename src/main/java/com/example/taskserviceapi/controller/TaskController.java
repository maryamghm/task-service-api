package com.example.taskserviceapi.controller;

import com.example.taskserviceapi.dto.*;
import com.example.taskserviceapi.entity.Task;
import com.example.taskserviceapi.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/task")
@RequiredArgsConstructor
public class TaskController {

    // Exposes task CRUD endpoints for authenticated users.
    private final TaskService taskService;

    /**
     * Create a new task for the authenticated user.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse createTask(
            Authentication authentication,
            @Valid @RequestBody CreateTaskRequest request) {
        return TaskResponse.from(taskService.createTask(authentication.getName(), request));
    }

    /**
     * List tasks, optionally paginated and sorted.
     */
    @GetMapping
    public TaskPageResponse listTasks(
            Authentication authentication,
            @Valid @ModelAttribute TaskListRequest params) {
        Page<Task> result = taskService.listTasksPage(
                authentication.getName(),
                PageRequest.of(params.page(), params.size(), params.getSort()));
        return TaskPageResponse.from(result);
    }

    /**
     * Fetch a single task by id for the authenticated user.
     */
    @GetMapping("/{id}")
    public TaskResponse getTask(Authentication authentication, @PathVariable Long id) {
        return TaskResponse.from(taskService.getTask(authentication.getName(), id));
    }

    /**
     * Update a task belonging to the authenticated user.
     */
    @PutMapping("/{id}")
    public TaskResponse updateTask(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskRequest request) {
        return TaskResponse.from(taskService.updateTask(authentication.getName(), id, request));
    }

    /**
     * Delete a task belonging to the authenticated user.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(Authentication authentication, @PathVariable Long id) {
        taskService.deleteTask(authentication.getName(), id);
    }
}
