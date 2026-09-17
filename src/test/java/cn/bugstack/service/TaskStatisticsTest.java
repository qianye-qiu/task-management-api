package cn.bugstack.service;

import cn.bugstack.entity.Task;
import cn.bugstack.entityEnum.TaskPriority;
import cn.bugstack.entityEnum.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class TaskStatisticsTest {

    private final TaskStatistics statistics = new TaskStatistics();

    private List<Task> tasks;

    private UUID projectA;

    private UUID projectB;

    private UUID userA;

    private UUID userB;

    @BeforeEach
    void setUp() {
        projectA = UUID.randomUUID();
        projectB = UUID.randomUUID();
        userA = UUID.randomUUID();
        userB = UUID.randomUUID();

        TaskStatus[] statuses = TaskStatus.values();
        TaskPriority[] priorities = TaskPriority.values();

        tasks = new ArrayList<>();

        for(int i = 0; i < 100; i++){
            Task task = new Task(
                    "Task" + i,
                    "Description" + i,
                    i % 2 == 0 ? projectA : projectB,
                    i % 2 == 0 ? userA : userB,
                    statuses[i % statuses.length],
                    priorities[i % priorities.length],
                    LocalDateTime.of(2026, 9, 1, 12, 0)
                            .plusDays(i)
            );
            tasks.add(task);
        }
    }

    @Test
    void shouldGroupTasksByStatus() {
        Map<TaskStatus, List<Task>> result = statistics.groupByStatus(tasks);

        assertEquals(25, result.get(TaskStatus.TODO).size());
        assertEquals(25, result.get(TaskStatus.IN_PROGRESS).size());
        assertEquals(25, result.get(TaskStatus.COMPLETED).size());
        assertEquals(25, result.get(TaskStatus.CANCELLED).size());

        assertTrue(
                result.get(TaskStatus.TODO)
                        .stream()
                        .allMatch(task -> task.getStatus() == TaskStatus.TODO)
        );
    }

    @Test
    void shouldGroupTasksByPriority(){
        Map<TaskPriority, List<Task>> result = statistics.groupByPriority(tasks);

        assertEquals(25, result.get(TaskPriority.LOW).size());
        assertEquals(25, result.get(TaskPriority.MEDIUM).size());
        assertEquals(25, result.get(TaskPriority.HIGH).size());
        assertEquals(25, result.get(TaskPriority.URGENT).size());
    }

    @Test
    void shouldFindOnlyUnfinishedOverdueTasks(){
        LocalDateTime now = LocalDateTime.of(2026, 9, 13, 12, 0);

        Task overdueTask = new Task(
                "逾期任务",
                "已经过期",
                projectA,
                userA,
                TaskStatus.TODO,
                TaskPriority.HIGH,
                now.minusDays(1)
        );

        Task futureTask = new Task(
                "未来任务",
                "还没有到期",
                projectA,
                userA,
                TaskStatus.TODO,
                TaskPriority.LOW,
                now.plusDays(1)
        );

        Task completedTask = new Task(
                "已完成任务",
                "虽然超过截止时间，但是已经完成",
                projectA,
                userA,
                TaskStatus.COMPLETED,
                TaskPriority.MEDIUM,
                now.minusDays(2)
        );

        List<Task> result = statistics.findOverdueTasks(
                List.of(overdueTask, futureTask, completedTask),
                now
        );

        assertEquals(1, result.size());
        assertSame(overdueTask, result.getFirst());
    }

    @Test
    void shouldCountTasksByProject() {
        Map<UUID, Long> result =
                statistics.countTasksByProject(tasks);

        assertEquals(50L, result.get(projectA));
        assertEquals(50L, result.get(projectB));
    }

    @Test
    void shouldCountTasksByUser() {
        Map<UUID, Long> result =
                statistics.countTasksByUser(tasks);

        assertEquals(50L, result.get(userA));
        assertEquals(50L, result.get(userB));
    }

    @Test
    void shouldFindEarliestDueTask() {
        Optional<Task> result =
                statistics.findEarliestDueTask(tasks);

        assertTrue(result.isPresent());
        assertEquals("Task0", result.get().getTitle());
        assertEquals(
                LocalDateTime.of(2026, 9, 1, 12, 0),
                result.get().getDueDate()
        );
    }
}
