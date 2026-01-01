package com.example.taskserviceapi.dto;

import com.example.taskserviceapi.exception.BadRequestException;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Sort;

import java.util.Set;

public record TaskListRequest(
        @Min(0) Integer page,
        @Min(1) Integer size,
        String sortProperty,
        String sortDir
) {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("createdAt", "priority");

    public TaskListRequest {
        page = page == null ? 0 : page;
        size = size == null ? 20 : size;
        sortDir = (sortDir == null || sortDir.isBlank()) ? "asc" : sortDir;
    }

    // Only allow sorting by createdAt and priority with explicit direction.
    public Sort getSort() {
        if (sortProperty == null || sortProperty.isBlank()) {
            return Sort.unsorted();
        }
        String trimmedSortBy = sortProperty.trim();
        if (!ALLOWED_SORT_FIELDS.contains(trimmedSortBy)) {
            throw new BadRequestException("Sorting is only supported by createdAt and priority");
        }
        Sort.Direction direction = Sort.Direction.fromOptionalString(sortDir)
                .orElseThrow(() -> new BadRequestException("Sort direction must be asc or desc"));
        return Sort.by(direction, trimmedSortBy);
    }
}
