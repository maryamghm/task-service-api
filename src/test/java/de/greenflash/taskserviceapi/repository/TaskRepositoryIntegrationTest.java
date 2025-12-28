package de.greenflash.taskserviceapi.repository;

import static de.greenflash.taskserviceapi.TestFixtures.USER_A_USERNAME;
import static de.greenflash.taskserviceapi.TestFixtures.USER_B_USERNAME;
import static de.greenflash.taskserviceapi.TestFixtures.userA;
import static de.greenflash.taskserviceapi.TestFixtures.userB;
import static org.assertj.core.api.Assertions.assertThat;

import de.greenflash.taskserviceapi.DockerAvailableCondition;
import de.greenflash.taskserviceapi.TestcontainersConfiguration;
import de.greenflash.taskserviceapi.entity.Task;
import de.greenflash.taskserviceapi.entity.TaskPriority;
import de.greenflash.taskserviceapi.entity.TaskStatus;
import de.greenflash.taskserviceapi.entity.User;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;

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
        User ownerA = userA();
        User ownerB = userB();

        ownerA = userRepository.save(ownerA);
        ownerB = userRepository.save(ownerB);

        Task taskA = new Task();
        taskA.setTitle("Task A");
        taskA.setDescription("Owned by A");
        taskA.setStatus(TaskStatus.TODO);
        taskA.setPriority(TaskPriority.HIGH);
        taskA.setOwner(ownerA);

        Task taskB = new Task();
        taskB.setTitle("Task B");
        taskB.setDescription("Owned by B");
        taskB.setStatus(TaskStatus.TODO);
        taskB.setPriority(TaskPriority.LOW);
        taskB.setOwner(ownerB);

        taskRepository.save(taskA);
        taskRepository.save(taskB);

        Page<Task> page = taskRepository.findByOwnerUsername(USER_A_USERNAME, PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getTitle()).isEqualTo("Task A");
    }

    @Test
    void findByIdAndOwnerUsernameScopesLookup() {
        User owner = userA();
        owner = userRepository.save(owner);

        Task task = new Task();
        task.setTitle("Scoped Task");
        task.setDescription("Owned by A");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.MEDIUM);
        task.setOwner(owner);
        task = taskRepository.save(task);

        Optional<Task> found = taskRepository.findByIdAndOwnerUsername(task.getId(), USER_A_USERNAME);
        Optional<Task> missing = taskRepository.findByIdAndOwnerUsername(task.getId(), USER_B_USERNAME);

        assertThat(found).isPresent();
        assertThat(missing).isEmpty();
    }
}
