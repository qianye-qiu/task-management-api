package cn.bugstack.controller;

import cn.bugstack.dto.CreateTaskRequest;
import cn.bugstack.dto.TaskResponse;
import cn.bugstack.dto.UpdateTaskRequest;
import cn.bugstack.entityEnum.TaskPriority;
import cn.bugstack.entityEnum.TaskStatus;
import cn.bugstack.exception.ResourceNotFoundException;
import cn.bugstack.repository.InMemoryTaskRepository;
import cn.bugstack.service.TaskService;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TaskControllerTest {
    @Test
    void createsUpdatesTransitionsAndDeletesTask() {
        TaskController controller = new TaskController(new TaskService(new InMemoryTaskRepository()));
        UUID projectId = UUID.randomUUID();
        TaskResponse created = controller.create(new CreateTaskRequest(
                "First", "description", projectId, null, TaskPriority.HIGH, null)).getBody();
        assertNotNull(created);

        assertEquals(TaskStatus.TODO, created.status());
        assertEquals(projectId, created.projectId());
        assertEquals(1, controller.list().size());
        assertEquals("First", controller.get(created.id()).title());

        TaskResponse updated = controller.update(created.id(),
                new UpdateTaskRequest("Second", null, null, null));
        assertEquals("Second", updated.title());
        assertEquals(TaskStatus.IN_PROGRESS, controller.start(created.id()).status());
        assertEquals(TaskStatus.COMPLETED, controller.complete(created.id()).status());

        controller.delete(created.id());
        assertThrows(ResourceNotFoundException.class, () -> controller.get(created.id()));
    }
}
