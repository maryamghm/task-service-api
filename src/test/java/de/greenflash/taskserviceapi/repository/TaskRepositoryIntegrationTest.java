package de.greenflash.taskserviceapi.repository;

import de.greenflash.taskserviceapi.DockerAvailableCondition;
import de.greenflash.taskserviceapi.TestcontainersConfiguration;
import de.greenflash.taskserviceapi.entity.Task;
import de.greenflash.taskserviceapi.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static de.greenflash.taskserviceapi.TestFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.test.database.replace=NONE"
})
@ExtendWith(DockerAvailableCondition.class)
class TaskRepositoryIntegrationTest {

    @Autowired
    private TaskRepository taskRepository;

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
