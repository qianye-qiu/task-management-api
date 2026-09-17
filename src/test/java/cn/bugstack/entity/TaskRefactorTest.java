package cn.bugstack.entity;

import cn.bugstack.dto.TaskResponse;
import cn.bugstack.entityEnum.TaskPriority;
import cn.bugstack.entityEnum.TaskStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TaskRefactorTest {

    @Test
    void creationInitializesTimestampsAndResponse() {
        LocalDateTime dueDate = LocalDateTime.of(2026, 9, 30, 12, 0);
        Task task = new Task("title", "description",
                UUID.randomUUID(), UUID.randomUUID(),
                TaskStatus.TODO, TaskPriority.HIGH, dueDate);


        assertNotNull(task.getId());
        assertNotNull(task.getCreatedAt());
        assertEquals(task.getCreatedAt(), task.getUpdatedAt());
        assertNull(task.getCompletedAt());

        TaskResponse response = TaskResponse.from(task);
        assertEquals(task.getId(), response.id());
        assertEquals(TaskStatus.TODO, response.status());
        assertEquals(TaskPriority.HIGH, response.priority());
        assertEquals(dueDate, response.dueDate());
        assertEquals(task.getCreatedAt(), response.createdAt());
        assertNull(response.completedAt());
    }

    @Test
    void completingTaskRecordsCompletionAndUpdateTime() {
        Task task = new Task("title", "description",
                UUID.randomUUID(), UUID.randomUUID(), TaskStatus.TODO,
                TaskPriority.LOW, LocalDateTime.of(2026, 9, 30, 12, 0));

        task.start();
        task.complete();

        assertEquals(TaskStatus.COMPLETED, task.getStatus());
        assertNotNull(task.getCompletedAt());
        assertEquals(task.getCompletedAt(), task.getUpdatedAt());
        assertEquals(task.getCompletedAt(), TaskResponse.from(task).completedAt());
    }

    @Test
    void changingTaskUpdatesTimestamp() {
        Task task = new Task("title", "description",
                UUID.randomUUID(), UUID.randomUUID(), TaskStatus.TODO,
                TaskPriority.LOW, LocalDateTime.of(2026, 9, 30, 12, 0));
        LocalDateTime previousUpdate = task.getUpdatedAt();

        task.changeTitle("new title");

        assertEquals("new title", task.getTitle());
        assertFalse(task.getUpdatedAt().isBefore(previousUpdate));
    }
}
