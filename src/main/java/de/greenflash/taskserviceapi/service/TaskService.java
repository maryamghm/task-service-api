package de.greenflash.taskserviceapi.service;

import de.greenflash.taskserviceapi.dto.CreateTaskRequest;
import de.greenflash.taskserviceapi.dto.UpdateTaskRequest;
import de.greenflash.taskserviceapi.entity.Task;
import de.greenflash.taskserviceapi.entity.User;
import de.greenflash.taskserviceapi.repository.TaskRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import de.greenflash.taskserviceapi.exception.ResourceNotFoundException;

@Service
@Transactional
@RequiredArgsConstructor
public class TaskService {

    // Handles task lifecycle operations scoped to the authenticated user.
    private final TaskRepository taskRepository;
    private final UserService userService;

    /**
     * Create a task owned by the given username.
     */
    public Task createTask(String ownerUsername, CreateTaskRequest request) {
        User owner = userService.findByUsername(ownerUsername);
        Task task = Task.from(request, owner);
        return taskRepository.save(task);
    }

    /**
     * Return a paginated view of the user's tasks.
     */
    @Transactional(readOnly = true)
    public Page<Task> listTasksPage(String ownerUsername, Pageable pageable) {
        return taskRepository.findByOwnerUsername(ownerUsername, pageable);
    }

    /**
     * Fetch a single task scoped to the user.
     */
    @Transactional(readOnly = true)
    public Task getTask(String ownerUsername, Long id) {
        return taskRepository.findByIdAndOwnerUsername(id, ownerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    }

    /**
     * Update a task owned by the user.
     */
    public Task updateTask(String ownerUsername, Long id, UpdateTaskRequest request) {
        Task task = taskRepository.findByIdAndOwnerUsername(id, ownerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        task.apply(request);
        return task;
    }

    /**
     * Delete a task owned by the user.
     */
    public void deleteTask(String ownerUsername, Long id) {
        Task task = taskRepository.findByIdAndOwnerUsername(id, ownerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        taskRepository.delete(task);
    }
}
