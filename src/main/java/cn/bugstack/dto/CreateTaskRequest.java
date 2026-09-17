package cn.bugstack.dto;

import cn.bugstack.entityEnum.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateTaskRequest(
        @NotBlank(message = "title must not be blank") String title,
        String description,
        @NotNull(message = "projectId is required") UUID projectId,
        UUID assigneeId,
        @NotNull(message = "priority is required") TaskPriority priority,
        LocalDateTime dueDate) {}
