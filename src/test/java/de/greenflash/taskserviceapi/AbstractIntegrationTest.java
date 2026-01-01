package de.greenflash.taskserviceapi;

import de.greenflash.taskserviceapi.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ExtendWith(DockerAvailableCondition.class)
public abstract class AbstractIntegrationTest {

    @Autowired
    protected TaskRepository taskRepository;

    @BeforeEach
    void resetTasks() {
        taskRepository.deleteAll();
    }

}
