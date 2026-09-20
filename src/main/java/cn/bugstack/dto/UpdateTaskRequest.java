package cn.bugstack.dto;

import cn.bugstack.entityEnum.TaskPriority;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

//被省略或为null的字段将保持不变。
public record UpdateTaskRequest(

        @Pattern(regexp = "(?s).*\\P{javaWhitespace}.*", message = "title must not be blank")
        @Size(max = 100, message = "title must be at most 100 characters")
        String title,

        @Size(max = 2000, message = "description must be at most 2000 characters")
        String description,

        TaskPriority priority,

        @Future(message = "dueDate must be in the future")
        LocalDateTime dueDate
) {}
