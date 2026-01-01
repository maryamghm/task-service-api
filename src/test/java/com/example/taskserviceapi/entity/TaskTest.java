package com.example.taskserviceapi.entity;

import org.junit.jupiter.api.Test;

import static com.example.taskserviceapi.TestFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;

class TaskTest {

    @Test
    void fromCreatesTaskWithDefaults() {
        User owner = userA();
        Task actual = Task.from(CREATE_TASK_REQUEST_WITHOUT_PRIORITY, owner);
        Task expected = newTask(TASK_TITLE, owner);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void applyUpdatesFields() {
        Task task = new Task();
        task.setTitle("Old");
        task.setDescription("Old desc");
        task.setStatus(TASK_STATUS);
        task.setPriority(TASK_PRIORITY);

        task.apply(UPDATE_TASK_REQUEST);

        assertThat(task.getTitle()).isEqualTo(UPDATE_TASK_REQUEST.title());
        assertThat(task.getDescription()).isEqualTo(UPDATE_TASK_REQUEST.description());
        assertThat(task.getStatus()).isEqualTo(UPDATE_TASK_REQUEST.status());
        assertThat(task.getPriority()).isEqualTo(UPDATE_TASK_REQUEST.priority());
    }
}
