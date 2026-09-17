package cn.bugstack.entity;

import cn.bugstack.entityEnum.TaskPriority;
import cn.bugstack.entityEnum.TaskStatus;
import cn.bugstack.exception.InvalidStateException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EntityTest {

    private Task createTask(){
        return new Task(
                "学习领域模型",
                "练习封装和行为方法",
                UUID.randomUUID(),
                UUID.randomUUID(),
                TaskStatus.TODO,
                TaskPriority.LOW,
                LocalDateTime.of(2026,9,6,12,0));
    }

    @Test
    public void shouldCreateTask() {
        Task task = createTask();

        assertEquals("学习领域模型", task.getTitle());
        assertEquals("练习封装和行为方法", task.getDescription());
        assertEquals(TaskStatus.TODO, task.getStatus());
        assertEquals(TaskPriority.LOW, task.getPriority());
        assertEquals(
                LocalDateTime.of(2026, 9, 6, 12, 0),
                task.getDueDate()
        );
    }

    @Test
    void shouldStartAndCompleteTask() {
        Task task = createTask();

        task.start();

        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());

        task.complete();

        assertEquals(TaskStatus.COMPLETED, task.getStatus());
    }

    @Test
    void shouldThrowExceptionWhenCompletingTodoTask() {
        Task task = createTask();

        InvalidStateException exception = assertThrows(
                InvalidStateException.class,
                task::complete
        );

        assertEquals(
                "Only IN_PROGRESS task can be completed",
                exception.getMessage()
        );

        // 发生异常后，状态不应该被改变
        assertEquals(TaskStatus.TODO, task.getStatus());
    }

    @Test
    void shouldThrowExceptionWhenStartingTaskTwice() {
        Task task = createTask();
        task.start();

        assertThrows(InvalidStateException.class, task::start);
        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
    }

    @Test
    void shouldThrowExceptionWhenCancellingCompletedTask() {
        Task task = createTask();
        task.start();
        task.complete();

        assertThrows(InvalidStateException.class, task::cancel);
        assertEquals(TaskStatus.COMPLETED, task.getStatus());
    }
}
