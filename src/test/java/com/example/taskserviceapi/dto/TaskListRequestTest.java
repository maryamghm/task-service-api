package com.example.taskserviceapi.dto;

import com.example.taskserviceapi.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import static com.example.taskserviceapi.TestFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TaskListRequestTest {

    @Test
    void defaultsMissingValues() {
        TaskListRequest request = new TaskListRequest(null, null, null, null);

        assertThat(request.page()).isZero();
        assertThat(request.size()).isEqualTo(20);
        assertThat(request.sortProperty()).isNull();
        assertThat(request.sortDir()).isEqualTo("asc");
    }

    @Test
    void normalizesBlankSortDir() {
        TaskListRequest request = new TaskListRequest(1, 10, SORT_CREATED_AT, "   ");

        assertThat(request.page()).isEqualTo(1);
        assertThat(request.size()).isEqualTo(10);
        assertThat(request.sortProperty()).isEqualTo(SORT_CREATED_AT);
        assertThat(request.sortDir()).isEqualTo(SORT_ASC);
    }

    @Test
    void getSortReturnsUnsortedWhenSortPropertyIsNull() {
        TaskListRequest request = new TaskListRequest(0, 10, null, SORT_ASC);

        Sort sort = request.getSort();

        assertThat(sort.isUnsorted()).isTrue();
    }

    @Test
    void getSortReturnsUnsortedWhenSortPropertyIsBlank() {
        TaskListRequest request = new TaskListRequest(0, 10, "   ", SORT_ASC);

        Sort sort = request.getSort();

        assertThat(sort.isUnsorted()).isTrue();
    }

    @Test
    void getSortRejectsUnsupportedField() {
        TaskListRequest request = new TaskListRequest(0, 10, SORT_INVALID_FIELD, SORT_DESC);

        assertThatThrownBy(request::getSort)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Sorting is only supported by createdAt and priority");
    }

    @Test
    void getSortRejectsInvalidDirection() {
        TaskListRequest request = new TaskListRequest(0, 10, SORT_CREATED_AT, SORT_INVALID_DIR);

        assertThatThrownBy(request::getSort)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Sort direction must be asc or desc");
    }

    @Test
    void getSortBuildsAscendingSortWithDefaultDirection() {
        TaskListRequest request = new TaskListRequest(0, 10, SORT_CREATED_AT, null);

        Sort sort = request.getSort();

        assertThat(sort.getOrderFor(SORT_CREATED_AT).getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void getSortBuildsDescendingSort() {
        TaskListRequest request = new TaskListRequest(0, 10, SORT_PRIORITY, SORT_DESC);

        Sort sort = request.getSort();

        assertThat(sort.getOrderFor(SORT_PRIORITY).getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void getSortTrimsSortProperty() {
        TaskListRequest request = new TaskListRequest(0, 10, "  " + SORT_CREATED_AT + "  ", SORT_ASC);

        Sort sort = request.getSort();

        assertThat(sort.getOrderFor(SORT_CREATED_AT).getDirection()).isEqualTo(Sort.Direction.ASC);
    }
}
