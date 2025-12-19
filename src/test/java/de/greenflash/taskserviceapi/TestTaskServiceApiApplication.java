package de.greenflash.taskserviceapi;

import org.springframework.boot.SpringApplication;

public class TestTaskServiceApiApplication {

    public static void main(String[] args) {
        SpringApplication.from(TaskServiceApiApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
