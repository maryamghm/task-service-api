package com.example.taskserviceapi.exception;

import static org.assertj.core.api.Assertions.assertThat;

import static com.example.taskserviceapi.TestFixtures.INVALID_CREATE_TASK_REQUEST;

import com.example.taskserviceapi.dto.CreateTaskRequest;
import jakarta.validation.ConstraintViolationException;
import java.lang.reflect.Method;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNotFoundReturns404() {
        ResponseEntity<ErrorResponse> response = handler.handleNotFound(
                new ResourceNotFoundException("Missing"));

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Missing");
    }

    @Test
    void handleBadRequestReturns400() {
        ResponseEntity<ErrorResponse> response = handler.handleBadRequest(
                new BadRequestException("Bad input"));

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Bad input");
    }

    @Test
    void handleValidationReturns400() throws Exception {
        BindingResult bindingResult = new BeanPropertyBindingResult(INVALID_CREATE_TASK_REQUEST, "request");
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter(), bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Validation failed");
    }

    @Test
    void handleConstraintViolationReturns400() {
        ConstraintViolationException ex = new ConstraintViolationException(Set.of());

        ResponseEntity<ErrorResponse> response = handler.handleConstraintViolation(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Validation failed");
    }

    private MethodParameter methodParameter() throws NoSuchMethodException {
        Method method = DummyController.class.getDeclaredMethod("create", CreateTaskRequest.class);
        return new MethodParameter(method, 0);
    }

    private static final class DummyController {
        @SuppressWarnings("unused")
        void create(CreateTaskRequest request) {
        }
    }
}
