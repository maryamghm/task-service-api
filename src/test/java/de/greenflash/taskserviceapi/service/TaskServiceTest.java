package de.greenflash.taskserviceapi.service;

import static de.greenflash.taskserviceapi.TestFixtures.CREATE_TASK_REQUEST;
import static de.greenflash.taskserviceapi.TestFixtures.TASK_TITLE;
import static de.greenflash.taskserviceapi.TestFixtures.UPDATE_TASK_REQUEST;
import static de.greenflash.taskserviceapi.TestFixtures.USER_A_USERNAME;
import static de.greenflash.taskserviceapi.TestFixtures.userA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.greenflash.taskserviceapi.entity.Task;
import de.greenflash.taskserviceapi.entity.TaskPriority;
import de.greenflash.taskserviceapi.entity.TaskStatus;
import de.greenflash.taskserviceapi.entity.User;
import de.greenflash.taskserviceapi.exception.ResourceNotFoundException;
import de.greenflash.taskserviceapi.repository.TaskRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

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
        saved.setId(42L);
        saved.setTitle(CREATE_TASK_REQUEST.title());
        saved.setDescription(CREATE_TASK_REQUEST.description());
        saved.setStatus(TaskStatus.TODO);
        saved.setPriority(TaskPriority.HIGH);
        saved.setOwner(owner);

        when(taskRepository.save(any(Task.class))).thenReturn(saved);

        Task response = taskService.createTask(USER_A_USERNAME, CREATE_TASK_REQUEST);

        assertThat(response.getId()).isEqualTo(42L);
        assertThat(response.getTitle()).isEqualTo(TASK_TITLE);
        assertThat(response.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(response.getPriority()).isEqualTo(TaskPriority.HIGH);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(captor.capture());
        Task persisted = captor.getValue();
        assertThat(persisted.getOwner()).isEqualTo(owner);
        assertThat(persisted.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(persisted.getPriority()).isEqualTo(TaskPriority.HIGH);
    }

    @Test
    void listTasksPageMapsEntities() {
        Task task = new Task();
        task.setId(7L);
        task.setTitle("Task 7");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.LOW);

        Page<Task> page = new PageImpl<>(List.of(task));
        when(taskRepository.findByOwnerUsername(USER_A_USERNAME, PageRequest.of(0, 5)))
                .thenReturn(page);

        Page<Task> result = taskService.listTasksPage(USER_A_USERNAME, PageRequest.of(0, 5));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Task 7");
    }

    @Test
    void getTaskThrowsWhenMissing() {
        when(taskRepository.findByIdAndOwnerUsername(99L, USER_A_USERNAME))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTask(USER_A_USERNAME, 99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Task not found");
    }

    @Test
    void updateTaskAppliesChanges() {
        Task existing = new Task();
        existing.setId(5L);
        existing.setTitle("Draft");
        existing.setDescription("Initial");
        existing.setStatus(TaskStatus.TODO);
        existing.setPriority(TaskPriority.LOW);

        when(taskRepository.findByIdAndOwnerUsername(5L, USER_A_USERNAME))
                .thenReturn(Optional.of(existing));

        Task response = taskService.updateTask(USER_A_USERNAME, 5L, UPDATE_TASK_REQUEST);

        assertThat(response.getTitle()).isEqualTo(UPDATE_TASK_REQUEST.title());
        assertThat(response.getDescription()).isEqualTo(UPDATE_TASK_REQUEST.description());
        assertThat(response.getStatus()).isEqualTo(UPDATE_TASK_REQUEST.status());
        assertThat(response.getPriority()).isEqualTo(UPDATE_TASK_REQUEST.priority());
    }

    @Test
    void deleteTaskRemovesEntity() {
        Task existing = new Task();
        existing.setId(12L);
        when(taskRepository.findByIdAndOwnerUsername(12L, USER_A_USERNAME))
                .thenReturn(Optional.of(existing));

        taskService.deleteTask(USER_A_USERNAME, 12L);

        verify(taskRepository).delete(existing);
    }
}
