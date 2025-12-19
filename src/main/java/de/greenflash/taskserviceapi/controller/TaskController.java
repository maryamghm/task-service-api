package de.greenflash.taskserviceapi.controller;

import de.greenflash.taskserviceapi.dto.CreateTaskRequest;
import de.greenflash.taskserviceapi.dto.TaskResponse;
import de.greenflash.taskserviceapi.dto.UpdateTaskRequest;
import de.greenflash.taskserviceapi.service.TaskService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

@RestController
@RequestMapping("/api/task")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            Authentication authentication,
            @Valid @RequestBody CreateTaskRequest request) {
        TaskResponse response = taskService.createTask(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<?> listTasks(
            Authentication authentication,
            @RequestParam Optional<Integer> page,
            @RequestParam Optional<Integer> size,
            @RequestParam(required = false) Sort sort) {
        Sort effectiveSort = Optional.ofNullable(sort).orElse(Sort.unsorted());
        // Return a Page when pagination params are present; otherwise return a full list.
        if (page.isPresent() && size.isPresent()) {
            Page<TaskResponse> result = taskService.listTasksPage(
                    authentication.getName(),
                    PageRequest.of(page.get(), size.get(), effectiveSort));
            return ResponseEntity.ok(result);
        }
        List<TaskResponse> tasks = taskService.listTasks(authentication.getName(), effectiveSort);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTask(Authentication authentication, @PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTask(authentication.getName(), id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskRequest request) {
        return ResponseEntity.ok(taskService.updateTask(authentication.getName(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(Authentication authentication, @PathVariable Long id) {
        taskService.deleteTask(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
