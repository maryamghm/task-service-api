package com.example.taskserviceapi.dto;

import com.example.taskserviceapi.entity.Task;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static com.example.taskserviceapi.TestFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;

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
