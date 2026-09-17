package cn.bugstack.repository;

import cn.bugstack.entity.Task;
import cn.bugstack.entityEnum.TaskPriority;
import cn.bugstack.entityEnum.TaskStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskRepositoryTest {

    @Test
    void shouldSaveAndFindTask(){
        Repository<java.util.UUID, Task> repository = new InMemoryTaskRepository();

        Task task = new Task("学习Repository", "使用Map保存Task",
                UUID.randomUUID(), UUID.randomUUID(),
                TaskStatus.TODO, TaskPriority.MEDIUM, LocalDateTime.now().plusDays(1));

        repository.save(task);

        Optional<Task> result = repository.findById(task.getId());

        assertTrue(result.isPresent());
        assertSame(task, result.get());
    }

    @Test
    void shouldReturnEmptyWhenTaskDoesNotExit(){
        Repository<java.util.UUID, Task> repository = new InMemoryTaskRepository();

        Optional<Task> result = repository.findById(java.util.UUID.randomUUID());

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldDeleteTask(){
        Repository<java.util.UUID, Task> repository = new InMemoryTaskRepository();

        Task task = new Task("待删除任务", "测试删除",
                UUID.randomUUID(), UUID.randomUUID(),
                TaskStatus.TODO, TaskPriority.LOW, LocalDateTime.now().plusDays(1));

        repository.save(task);
        repository.delete(task.getId());

        assertTrue(repository.findById(task.getId()).isEmpty());
    }

    @Test
    void shouldRejectNullTask(){
        Repository<java.util.UUID, Task> repository = new InMemoryTaskRepository();

        assertThrows(IllegalArgumentException.class, () -> repository.save(null));
    }
}
