package cn.bugstack.entity;

import cn.bugstack.entityEnum.TaskPriority;
import cn.bugstack.entityEnum.TaskStatus;
import cn.bugstack.exception.BusinessRuleViolationException;
import cn.bugstack.exception.InvalidStateException;
import cn.bugstack.exception.ResourceNotFoundException;
import cn.bugstack.repository.InMemoryProjectRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TaskExceptionTest {

    @Test
    void shouldRejectCompletingTodoTask() {
        Task task = new Task(
                "学习异常模型",
                "测试非法状态",
                UUID.randomUUID(),
                UUID.randomUUID(),
                TaskStatus.TODO,
                TaskPriority.MEDIUM,
                LocalDateTime.now().plusDays(1)
        );

        InvalidStateException exception = assertThrows(
                InvalidStateException.class,
                task::complete
        );

        assertEquals(
                "Only IN_PROGRESS task can be completed",
                exception.getMessage()
        );

        assertEquals(TaskStatus.TODO, task.getStatus());
    }

    @Test
    void shouldRejectBlankTaskTitle() {
        assertThrows(
                BusinessRuleViolationException.class,
                () -> new Task(
                        " ",
                        "测试空标题",
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        TaskStatus.TODO,
                        TaskPriority.LOW,
                        LocalDateTime.now().plusDays(1)
                )
        );
    }

    @Test
    void shouldThrowWhenTaskDoesNotExist() {
        UUID taskId = UUID.randomUUID();

        InMemoryProjectRepository taskRepository = new InMemoryProjectRepository();
        assertThrows(
                ResourceNotFoundException.class,
                () -> taskRepository.findById(taskId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Task", taskId)
                        )
        );
    }
}
