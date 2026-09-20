package cn.bugstack.dto;

import cn.bugstack.entityEnum.TaskPriority;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateTaskRequest(
        @NotBlank(message = "title must not be blank")
        @Size(max = 100, message = "title must be at most 100characters")
        String title,

        @Size(max = 2000, message = "description must be at most 2000 characters")
        String description,

        @NotNull(message = "projectId is required")
        UUID projectId,

        UUID assigneeId,

        @NotNull(message = "priority is required")
        TaskPriority priority,

        @Future(message = "dueDate must be in the future")
        LocalDateTime dueDate) {}
