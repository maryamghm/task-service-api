package com.example.taskserviceapi.dto;

import static com.example.taskserviceapi.TestFixtures.TASK_ID;
import static com.example.taskserviceapi.TestFixtures.TASK_PRIORITY;
import static com.example.taskserviceapi.TestFixtures.TASK_STATUS;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.taskserviceapi.entity.Task;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class TaskResponseTest {

    @Test
    void mapsTaskFields() {
        Task task = new Task();
        task.setId(TASK_ID);
        task.setTitle("Title");
        task.setDescription("Desc");
        task.setStatus(TASK_STATUS);
        task.setPriority(TASK_PRIORITY);
        Instant createdAt = Instant.parse("2024-01-01T10:15:30Z");
        Instant updatedAt = Instant.parse("2024-01-02T10:15:30Z");
        task.setCreatedAt(createdAt);
        task.setUpdatedAt(updatedAt);

        TaskResponse response = TaskResponse.from(task);

        assertThat(response.id()).isEqualTo(TASK_ID);
        assertThat(response.title()).isEqualTo("Title");
        assertThat(response.description()).isEqualTo("Desc");
        assertThat(response.status()).isEqualTo(TASK_STATUS);
        assertThat(response.priority()).isEqualTo(TASK_PRIORITY);
        assertThat(response.createdAt()).isEqualTo(createdAt);
        assertThat(response.updatedAt()).isEqualTo(updatedAt);
    }
}
