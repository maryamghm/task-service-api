package com.example.taskserviceapi.service;

import com.example.taskserviceapi.entity.Task;
import com.example.taskserviceapi.entity.User;
import com.example.taskserviceapi.exception.ResourceNotFoundException;
import com.example.taskserviceapi.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static com.example.taskserviceapi.TestFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private TaskService taskService;

    @Test
    void createTaskPersistsDefaultsAndMapsResponse() {
        User owner = userA();
        when(userService.findByUsername(USER_A_USERNAME)).thenReturn(owner);

        Task saved = new Task();
        saved.setId(TASK_ID);
        saved.setTitle(CREATE_TASK_REQUEST.title());
        saved.setDescription(CREATE_TASK_REQUEST.description());
        saved.setStatus(TASK_STATUS);
        saved.setPriority(TASK_PRIORITY);
        saved.setOwner(owner);

        when(taskRepository.save(any(Task.class))).thenReturn(saved);

        Task response = taskService.createTask(USER_A_USERNAME, CREATE_TASK_REQUEST);

        assertThat(response).isEqualTo(saved);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(captor.capture());
        Task persisted = captor.getValue();
        assertThat(persisted.getOwner()).isEqualTo(owner);
        assertThat(persisted.getStatus()).isEqualTo(TASK_STATUS);
        assertThat(persisted.getPriority()).isEqualTo(TASK_PRIORITY);
    }

    @Test
    void listTasksPageMapsEntities() {
        Task task = new Task();
        task.setId(TASK_ID);
        task.setTitle("Task 7");
        task.setStatus(TASK_STATUS);
        task.setPriority(TASK_PRIORITY);

        Page<Task> page = new PageImpl<>(List.of(task));
        when(taskRepository.findByOwnerUsername(USER_A_USERNAME, PageRequest.of(0, 5)))
                .thenReturn(page);

        Page<Task> result = taskService.listTasksPage(USER_A_USERNAME, PageRequest.of(0, 5));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Task 7");
    }

    @Test
    void getTaskThrowsWhenMissing() {
        when(taskRepository.findByIdAndOwnerUsername(TASK_ID_NOT_FOUND, USER_A_USERNAME))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTask(USER_A_USERNAME, TASK_ID_NOT_FOUND))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Task not found");
    }

    @Test
    void updateTaskAppliesChanges() {
        Task existing = new Task();
        existing.setId(TASK_ID);
        existing.setTitle("Draft");
        existing.setDescription("Initial");
        existing.setStatus(TASK_STATUS);
        existing.setPriority(TASK_PRIORITY);

        when(taskRepository.findByIdAndOwnerUsername(TASK_ID, USER_A_USERNAME))
                .thenReturn(Optional.of(existing));

        Task response = taskService.updateTask(USER_A_USERNAME, TASK_ID, UPDATE_TASK_REQUEST);

        assertThat(response.getTitle()).isEqualTo(UPDATE_TASK_REQUEST.title());
        assertThat(response.getDescription()).isEqualTo(UPDATE_TASK_REQUEST.description());
        assertThat(response.getStatus()).isEqualTo(UPDATE_TASK_REQUEST.status());
        assertThat(response.getPriority()).isEqualTo(UPDATE_TASK_REQUEST.priority());
    }

    @Test
    void deleteTaskRemovesEntity() {
        Task existing = new Task();
        existing.setId(TASK_ID);
        when(taskRepository.findByIdAndOwnerUsername(TASK_ID, USER_A_USERNAME))
                .thenReturn(Optional.of(existing));

        taskService.deleteTask(USER_A_USERNAME, TASK_ID);

        verify(taskRepository).delete(existing);
    }
}
