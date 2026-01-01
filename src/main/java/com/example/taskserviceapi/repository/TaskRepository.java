package com.example.taskserviceapi.repository;

import com.example.taskserviceapi.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {
    Page<Task> findByOwnerUsername(String ownerUsername, Pageable pageable);

    Optional<Task> findByIdAndOwnerUsername(Long id, String ownerUsername);
}
