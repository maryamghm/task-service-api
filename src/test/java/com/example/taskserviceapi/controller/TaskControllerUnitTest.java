package com.example.taskserviceapi.controller;

import com.example.taskserviceapi.AbstractMockMvcUnitTest;
import com.example.taskserviceapi.dto.CreateTaskRequest;
import com.example.taskserviceapi.entity.Task;
import com.example.taskserviceapi.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.example.taskserviceapi.TestFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TaskControllerUnitTest extends AbstractMockMvcUnitTest {

    @Mock
    private TaskService taskService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = buildStandaloneMockMvc(new TaskController(taskService));
    }

    @Test
    void createTaskMapsResponse() throws Exception {
        Task task = new Task();
        task.setId(TASK_ID);
        task.setTitle(TASK_TITLE);
        task.setDescription(TASK_DESCRIPTION);
        task.setStatus(TASK_STATUS);
        task.setPriority(TASK_PRIORITY);

        when(taskService.createTask(eq(USER_A_USERNAME), any(CreateTaskRequest.class)))
                .thenReturn(task);

        String payload = """
                {
                  "title": "%s",
                  "description": "%s",
                  "priority": "%s"
                }
                """.formatted(TASK_TITLE, TASK_DESCRIPTION, TASK_PRIORITY.name());

        mockMvc.perform(post("/api/task")
                        .principal(userAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(TASK_ID))
                .andExpect(jsonPath("$.title").value(TASK_TITLE))
                .andExpect(jsonPath("$.description").value(TASK_DESCRIPTION))
                .andExpect(jsonPath("$.priority").value(TASK_PRIORITY.name()));

        verify(taskService).createTask(USER_A_USERNAME,
                CREATE_TASK_REQUEST);
    }

    @Test
    void listTasksMapsPageContent() throws Exception {
        Task task = new Task();
        task.setId(TASK_ID);
        task.setTitle(TASK_TITLE);
        task.setStatus(TASK_STATUS);
        task.setPriority(TASK_PRIORITY);

        Page<Task> page = new PageImpl<>(List.of(task));
        when(taskService.listTasksPage(eq(USER_A_USERNAME), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/task")
                        .principal(userAuth())
                        .param("page", "1")
                        .param("size", "5")
                        .param("sortProperty", "createdAt")
                        .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(TASK_ID))
                .andExpect(jsonPath("$.content[0].title").value(TASK_TITLE));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(taskService).listTasksPage(eq(USER_A_USERNAME), pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(1);
        assertThat(pageable.getSort().getOrderFor("createdAt").getDirection())
                .isEqualTo(org.springframework.data.domain.Sort.Direction.DESC);
    }

    @Test
    void listTasksRejectsInvalidSort() throws Exception {
        mockMvc.perform(get("/api/task")
                        .principal(userAuth())
                        .param("sortProperty", "title")
                        .param("sortDir", "desc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Sorting is only supported by createdAt and priority"));

        verifyNoInteractions(taskService);
    }

    @Test
    void listTasksRejectsInvalidSortDirection() throws Exception {
        mockMvc.perform(get("/api/task")
                        .principal(userAuth())
                        .param("sortProperty", "createdAt")
                        .param("sortDir", "sideways"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Sort direction must be asc or desc"));

        verifyNoInteractions(taskService);
    }

    @Test
    void listTasksRejectsInvalidPagination() throws Exception {
        mockMvc.perform(get("/api/task")
                        .principal(userAuth())
                        .param("page", "-1")
                        .param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Validation failed"));

        verifyNoInteractions(taskService);
    }

    @Test
    void getTaskMapsResponse() throws Exception {
        Task task = new Task();
        task.setId(TASK_ID);
        task.setTitle(TASK_TITLE);
        task.setStatus(TASK_STATUS);
        task.setPriority(TASK_PRIORITY);

        when(taskService.getTask(USER_A_USERNAME, TASK_ID)).thenReturn(task);

        mockMvc.perform(get("/api/task/" + TASK_ID)
                        .principal(userAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TASK_ID))
                .andExpect(jsonPath("$.title").value(TASK_TITLE));
    }

    @Test
    void updateTaskMapsResponse() throws Exception {
        Task updated = new Task();
        updated.setId(TASK_ID);
        updated.setTitle(UPDATE_TASK_REQUEST.title());
        updated.setDescription(UPDATE_TASK_REQUEST.description());
        updated.setStatus(UPDATE_TASK_REQUEST.status());
        updated.setPriority(UPDATE_TASK_REQUEST.priority());

        when(taskService.updateTask(USER_A_USERNAME, TASK_ID, UPDATE_TASK_REQUEST))
                .thenReturn(updated);

        String payload = """
                {
                  "title": "%s",
                  "description": "%s",
                  "status": "%s",
                  "priority": "%s"
                }
                """.formatted(
                UPDATE_TASK_REQUEST.title(),
                UPDATE_TASK_REQUEST.description(),
                UPDATE_TASK_REQUEST.status(),
                UPDATE_TASK_REQUEST.priority());

        mockMvc.perform(put("/api/task/" + TASK_ID)
                        .principal(userAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(UPDATE_TASK_REQUEST.title()))
                .andExpect(jsonPath("$.status").value(UPDATE_TASK_REQUEST.status().name()));
    }

    @Test
    void deleteTaskDelegatesToService() throws Exception {
        mockMvc.perform(delete("/api/task/" + TASK_ID)
                        .principal(userAuth()))
                .andExpect(status().isNoContent());

        verify(taskService).deleteTask(USER_A_USERNAME, TASK_ID);
    }

    private Authentication userAuth() {
        return new UsernamePasswordAuthenticationToken(USER_A_USERNAME, "N/A");
    }

}
