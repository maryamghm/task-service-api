package de.greenflash.taskserviceapi;

import de.greenflash.taskserviceapi.dto.CreateTaskRequest;
import de.greenflash.taskserviceapi.dto.UpdateTaskRequest;
import de.greenflash.taskserviceapi.entity.Task;
import de.greenflash.taskserviceapi.entity.TaskPriority;
import de.greenflash.taskserviceapi.entity.TaskStatus;
import de.greenflash.taskserviceapi.entity.User;

public final class TestFixtures {

    public static final String USER_A_USERNAME = "UserA";
    public static final String USER_B_USERNAME = "UserB";
    public static final String USER_A_PASSWORD = "UserA";
    public static final String USER_B_PASSWORD = "UserB";

    public static final String TASK_TITLE = "Write tests";
    public static final String TASK_DESCRIPTION = "Cover task controller";
    public static final TaskPriority TASK_PRIORITY = TaskPriority.MEDIUM;
    public static final TaskStatus TASK_STATUS = TaskStatus.TODO;
    public static final Long TASK_ID = 10L;
    public static final Long TASK_ID_NOT_FOUND = 99L;

    public static final CreateTaskRequest CREATE_TASK_REQUEST = new CreateTaskRequest(
            TASK_TITLE,
            TASK_DESCRIPTION,
            TASK_PRIORITY);

    public static final CreateTaskRequest CREATE_TASK_REQUEST_WITHOUT_PRIORITY = new CreateTaskRequest(
            TASK_TITLE,
            TASK_DESCRIPTION,
            null);

    public static final CreateTaskRequest INVALID_CREATE_TASK_REQUEST = new CreateTaskRequest(
            " ",
            TASK_DESCRIPTION,
            TASK_PRIORITY);

    public static final UpdateTaskRequest UPDATE_TASK_REQUEST = new UpdateTaskRequest(
            "Final title",
            "Updated description",
            TASK_STATUS,
            TASK_PRIORITY);

    public static final String JWT_SECRET = "test-secret-should-be-long-enough-for-hmac-signing-32bytes";

    private TestFixtures() {
    }

    public static User userA() {
        return newUser(USER_A_USERNAME, USER_A_PASSWORD);
    }

    public static User userB() {
        return newUser(USER_B_USERNAME, USER_B_PASSWORD);
    }

    private static User newUser(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        return user;
    }

    public static Task newTask(String title, User owner) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription(TASK_DESCRIPTION);
        task.setStatus(TASK_STATUS);
        task.setPriority(TASK_PRIORITY);
        task.setOwner(owner);
        return task;
    }
}
