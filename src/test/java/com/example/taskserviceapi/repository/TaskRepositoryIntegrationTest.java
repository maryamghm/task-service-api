package com.example.taskserviceapi.repository;

import com.example.taskserviceapi.AbstractIntegrationTest;
import com.example.taskserviceapi.entity.Task;
import com.example.taskserviceapi.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import static com.example.taskserviceapi.TestFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;

class TaskRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByOwnerUsernameReturnsScopedTasks() {
        User ownerA = userRepository.findByUsername(USER_A_USERNAME).get();
        User ownerB = userRepository.findByUsername(USER_B_USERNAME).get();

        Task taskA = new Task();
        taskA.setTitle("Task A");
        taskA.setDescription("Owned by A");
        taskA.setStatus(TASK_STATUS);
        taskA.setPriority(TASK_PRIORITY);
        taskA.setOwner(ownerA);

        Task taskB = new Task();
        taskB.setTitle("Task B");
        taskB.setDescription("Owned by B");
        taskB.setStatus(TASK_STATUS);
        taskB.setPriority(TASK_PRIORITY);
        taskB.setOwner(ownerB);

        taskRepository.save(taskA);
        taskRepository.save(taskB);

        Page<Task> page = taskRepository.findByOwnerUsername(USER_A_USERNAME, PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getTitle()).isEqualTo("Task A");
    }

    @Test
    void findByIdAndOwnerUsernameScopesLookup() {
        User owner = userRepository.findByUsername(USER_A_USERNAME).get();

        Task task = new Task();
        task.setTitle("Scoped Task");
        task.setDescription("Owned by A");
        task.setStatus(TASK_STATUS);
        task.setPriority(TASK_PRIORITY);
        task.setOwner(owner);
        task = taskRepository.save(task);

        Optional<Task> found = taskRepository.findByIdAndOwnerUsername(task.getId(), USER_A_USERNAME);
        Optional<Task> missing = taskRepository.findByIdAndOwnerUsername(task.getId(), USER_B_USERNAME);

        assertThat(found).isPresent();
        assertThat(missing).isEmpty();
    }
}
