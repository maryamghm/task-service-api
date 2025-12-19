package de.greenflash.taskserviceapi.service;

import de.greenflash.taskserviceapi.dto.CreateTaskRequest;
import de.greenflash.taskserviceapi.dto.TaskResponse;
import de.greenflash.taskserviceapi.dto.UpdateTaskRequest;
import de.greenflash.taskserviceapi.entity.Task;
import de.greenflash.taskserviceapi.entity.User;
import de.greenflash.taskserviceapi.repository.TaskRepository;
import java.util.List;
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
        Task task = Task.from(request, owner);
        Task saved = taskRepository.save(task);
        return TaskResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> listTasksPage(String ownerUsername, Pageable pageable) {
        return taskRepository.findByOwnerUsername(ownerUsername, pageable)
                .map(TaskResponse::from);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> listTasks(String ownerUsername, Sort sort) {
        List<Task> tasks = taskRepository.findByOwnerUsername(ownerUsername, sort);
        return toResponses(tasks);
    }

    @Transactional(readOnly = true)
    public TaskResponse getTask(String ownerUsername, Long id) {
        Task task = taskRepository.findByIdAndOwnerUsername(id, ownerUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        return TaskResponse.from(task);
    }

    public TaskResponse updateTask(String ownerUsername, Long id, UpdateTaskRequest request) {
        Task task = taskRepository.findByIdAndOwnerUsername(id, ownerUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        task.apply(request);
        return TaskResponse.from(task);
    }

    public void deleteTask(String ownerUsername, Long id) {
        Task task = taskRepository.findByIdAndOwnerUsername(id, ownerUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        taskRepository.delete(task);
    }

    private List<TaskResponse> toResponses(List<Task> tasks) {
        return tasks.stream()
                .map(TaskResponse::from)
                .collect(Collectors.toList());
    }
}
