package de.greenflash.taskserviceapi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ExtendWith(DockerAvailableCondition.class)
class TaskServiceApiApplicationTests {

    @Test
    void contextLoads() {
    }

}
