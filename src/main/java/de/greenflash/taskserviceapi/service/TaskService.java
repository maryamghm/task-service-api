package de.greenflash.taskserviceapi.service;

import de.greenflash.taskserviceapi.dto.CreateTaskRequest;
import de.greenflash.taskserviceapi.dto.TaskResponse;
import de.greenflash.taskserviceapi.dto.UpdateTaskRequest;
import de.greenflash.taskserviceapi.entity.Task;
import de.greenflash.taskserviceapi.entity.TaskPriority;
import de.greenflash.taskserviceapi.entity.TaskStatus;
import de.greenflash.taskserviceapi.entity.User;
import de.greenflash.taskserviceapi.repository.TaskRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserService userService;

    public TaskResponse createTask(String ownerUsername, CreateTaskRequest request) {
        User owner = userService.findByUsername(ownerUsername);
        Task task = new Task();
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(TaskStatus.TODO);
        task.setPriority(Optional.ofNullable(request.priority()).orElse(TaskPriority.MEDIUM));
        task.setOwner(owner);
        Task saved = taskRepository.save(task);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> listTasksPage(String ownerUsername, Pageable pageable) {
        return taskRepository.findByOwner_Username(ownerUsername, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> listTasks(String ownerUsername, Sort sort) {
        List<Task> tasks = taskRepository.findByOwner_Username(ownerUsername, sort);
        return toResponses(tasks);
    }

    @Transactional(readOnly = true)
    public TaskResponse getTask(String ownerUsername, Long id) {
        Task task = taskRepository.findByIdAndOwner_Username(id, ownerUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        return toResponse(task);
    }

    public TaskResponse updateTask(String ownerUsername, Long id, UpdateTaskRequest request) {
        Task task = taskRepository.findByIdAndOwner_Username(id, ownerUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(request.status());
        task.setPriority(request.priority());
        return toResponse(task);
    }

    public void deleteTask(String ownerUsername, Long id) {
        Task task = taskRepository.findByIdAndOwner_Username(id, ownerUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        taskRepository.delete(task);
    }

    private List<TaskResponse> toResponses(List<Task> tasks) {
        return tasks.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private TaskResponse toResponse(Task task) {
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
