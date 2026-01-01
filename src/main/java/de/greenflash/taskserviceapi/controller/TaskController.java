package de.greenflash.taskserviceapi.controller;

import de.greenflash.taskserviceapi.dto.CreateTaskRequest;
import de.greenflash.taskserviceapi.dto.TaskPageResponse;
import de.greenflash.taskserviceapi.dto.TaskResponse;
import de.greenflash.taskserviceapi.dto.UpdateTaskRequest;
import de.greenflash.taskserviceapi.entity.Task;
import de.greenflash.taskserviceapi.exception.BadRequestException;
import de.greenflash.taskserviceapi.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

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
    public TaskPageResponse listTasks(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sortProperty,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort sort = resolveSort(sortProperty, sortDir);
        Page<Task> result = taskService.listTasksPage(
                authentication.getName(),
                PageRequest.of(page, size, sort));
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

    // Only allow sorting by createdAt and priority with explicit direction.
    private Sort resolveSort(String sortProperty, String sortDir) {
        if (sortProperty == null || sortProperty.isBlank()) {
            return Sort.unsorted();
        }
        String trimmedSortBy = sortProperty.trim();
        if (!ALLOWED_SORT_FIELDS.contains(trimmedSortBy)) {
            throw new BadRequestException("Sorting is only supported by createdAt and priority");
        }
        Sort.Direction direction = Sort.Direction.fromOptionalString(sortDir)
                .orElseThrow(() -> new BadRequestException("Sort direction must be asc or desc"));
        return Sort.by(direction, trimmedSortBy);
    }
}
