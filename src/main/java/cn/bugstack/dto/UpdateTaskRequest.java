package cn.bugstack.dto;

import cn.bugstack.entityEnum.TaskPriority;

import java.time.LocalDateTime;

public record UpdateTaskRequest(
        String title,
        String description,
        TaskPriority priority,
        LocalDateTime dueDate
) {}
