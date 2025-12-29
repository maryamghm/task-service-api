package de.greenflash.taskserviceapi.controller;

import static de.greenflash.taskserviceapi.TestFixtures.CREATE_TASK_REQUEST;
import static de.greenflash.taskserviceapi.TestFixtures.TASK_DESCRIPTION;
import static de.greenflash.taskserviceapi.TestFixtures.TASK_ID;
import static de.greenflash.taskserviceapi.TestFixtures.TASK_PRIORITY;
import static de.greenflash.taskserviceapi.TestFixtures.TASK_STATUS;
import static de.greenflash.taskserviceapi.TestFixtures.TASK_TITLE;
import static de.greenflash.taskserviceapi.TestFixtures.UPDATE_TASK_REQUEST;
import static de.greenflash.taskserviceapi.TestFixtures.USER_A_USERNAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.greenflash.taskserviceapi.dto.CreateTaskRequest;
import de.greenflash.taskserviceapi.entity.Task;
import de.greenflash.taskserviceapi.exception.GlobalExceptionHandler;
import de.greenflash.taskserviceapi.service.TaskService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.SpringDataJacksonConfiguration;
import org.springframework.data.web.config.SpringDataWebSettings;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@ExtendWith(MockitoExtension.class)
class TaskControllerUnitTest {

    @Mock
    private TaskService taskService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();
        conversionService.addConverter(new StringToSortConverter());
        SpringDataWebSettings springDataWebSettings =
                new SpringDataWebSettings(EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO);
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .registerModule(new SpringDataJacksonConfiguration.PageModule(springDataWebSettings));
        MappingJackson2HttpMessageConverter messageConverter =
                new MappingJackson2HttpMessageConverter(objectMapper);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new TaskController(taskService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new SortHandlerMethodArgumentResolver())
                .setMessageConverters(messageConverter)
                .setConversionService(conversionService)
                .setValidator(validator)
                .build();
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
                        .param("sort", "createdAt,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(TASK_ID))
                .andExpect(jsonPath("$.content[0].title").value(TASK_TITLE));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(taskService).listTasksPage(eq(USER_A_USERNAME), pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(1);
        assertThat(pageable.getPageSize()).isEqualTo(5);
        assertThat(pageable.getSort().getOrderFor("createdAt")).isNotNull();
    }

    @Test
    void listTasksRejectsInvalidSort() throws Exception {
        mockMvc.perform(get("/api/task")
                        .principal(userAuth())
                        .param("sort", "title,desc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Sorting is only supported by createdAt and priority"));

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

    private static final class StringToSortConverter implements Converter<String, Sort> {
        @Override
        public Sort convert(String source) {
            if (source == null || source.isBlank()) {
                return Sort.unsorted();
            }
            String[] parts = source.split(",");
            Sort sort = Sort.unsorted();
            for (int i = 0; i < parts.length; i += 2) {
                String property = parts[i].trim();
                if (property.isEmpty()) {
                    continue;
                }
                Sort.Direction direction = Sort.Direction.ASC;
                if (i + 1 < parts.length) {
                    direction = Sort.Direction.fromOptionalString(parts[i + 1].trim())
                            .orElse(Sort.Direction.ASC);
                }
                Sort next = Sort.by(new Sort.Order(direction, property));
                sort = sort.and(next);
            }
            return sort;
        }
    }
}
