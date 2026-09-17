package cn.bugstack.entity;

import cn.bugstack.entityEnum.*;
import cn.bugstack.exception.BusinessRuleViolationException;
import cn.bugstack.service.TaskStatistics;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ReviewFixTest {
    @Test
    void rejectsBlankTitleOnUpdateWithoutChangingTask() {
        Task task = new Task("valid", "", UUID.randomUUID(), null, TaskStatus.TODO, TaskPriority.LOW, null);
        assertThrows(BusinessRuleViolationException.class, () -> task.changeTitle("  "));
        assertEquals("valid", task.getTitle());
    }

    @Test
    void userHasIdentityAndTimestamps() {
        User user = new User("name", "mail", "secret");
        assertNotNull(user.getId());
        assertNotNull(user.getCreateTime());
        assertEquals(user.getCreateTime(), user.getUpdateTime());
        user.disable();
        assertFalse(user.getUpdateTime().isBefore(user.getCreateTime()));
    }

    @Test
    void projectChangesRefreshTimestamp() {
        Project project = new Project("name", "", UUID.randomUUID(), ProjectStatus.ACTIVE);
        LocalDateTime before = project.getUpdateTime();
        project.archived();
        assertFalse(project.getUpdateTime().isBefore(before));
    }

    @Test
    void statisticsIgnoreUnsetOptionalFields() {
        Task task = new Task("valid", "", UUID.randomUUID(), null, TaskStatus.TODO, TaskPriority.LOW, null);
        TaskStatistics statistics = new TaskStatistics();
        assertTrue(statistics.findOverdueTasks(List.of(task), LocalDateTime.now()).isEmpty());
        assertTrue(statistics.findEarliestDueTask(List.of(task)).isEmpty());
        assertTrue(statistics.countTasksByUser(List.of(task)).isEmpty());
    }
}
