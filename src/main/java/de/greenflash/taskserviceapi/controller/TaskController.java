package de.greenflash.taskserviceapi.controller;

import de.greenflash.taskserviceapi.dto.CreateTaskRequest;
import de.greenflash.taskserviceapi.dto.TaskResponse;
import de.greenflash.taskserviceapi.dto.UpdateTaskRequest;
import de.greenflash.taskserviceapi.service.TaskService;
import jakarta.validation.Valid;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;
import de.greenflash.taskserviceapi.exception.BadRequestException;

@RestController
@RequestMapping("/api/task")
@RequiredArgsConstructor
public class TaskController {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("createdAt", "priority");

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
    public Page<TaskResponse> listTasks(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Sort sort) {
        Sort effectiveSort = Optional.ofNullable(sort).orElse(Sort.unsorted());
        validateSort(effectiveSort);
        return taskService.listTasksPage(
                authentication.getName(),
                PageRequest.of(page, size, effectiveSort))
                .map(TaskResponse::from);
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

    // Only allow sorting by createdAt and/or priority.
    private void validateSort(Sort sort) {
        for (Sort.Order order : sort) {
            if (!ALLOWED_SORT_FIELDS.contains(order.getProperty())) {
                throw new BadRequestException("Sorting is only supported by createdAt and priority");
            }
        }
    }
}
