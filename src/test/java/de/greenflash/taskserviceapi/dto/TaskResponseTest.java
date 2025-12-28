package de.greenflash.taskserviceapi.dto;

import static org.assertj.core.api.Assertions.assertThat;

import de.greenflash.taskserviceapi.entity.Task;
import de.greenflash.taskserviceapi.entity.TaskPriority;
import de.greenflash.taskserviceapi.entity.TaskStatus;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class TaskResponseTest {

    @Test
    void mapsTaskFields() {
        Task task = new Task();
        task.setId(15L);
        task.setTitle("Title");
        task.setDescription("Desc");
        task.setStatus(TaskStatus.IN_PROGRESS);
        task.setPriority(TaskPriority.HIGH);
        Instant createdAt = Instant.parse("2024-01-01T10:15:30Z");
        Instant updatedAt = Instant.parse("2024-01-02T10:15:30Z");
        task.setCreatedAt(createdAt);
        task.setUpdatedAt(updatedAt);

        TaskResponse response = TaskResponse.from(task);

        assertThat(response.id()).isEqualTo(15L);
        assertThat(response.title()).isEqualTo("Title");
        assertThat(response.description()).isEqualTo("Desc");
        assertThat(response.status()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(response.priority()).isEqualTo(TaskPriority.HIGH);
        assertThat(response.createdAt()).isEqualTo(createdAt);
        assertThat(response.updatedAt()).isEqualTo(updatedAt);
    }
}
