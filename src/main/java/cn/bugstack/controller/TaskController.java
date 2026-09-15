package cn.bugstack.controller;

import cn.bugstack.dto.TaskResponse;
import cn.bugstack.entity.Task;
import cn.bugstack.entityEnum.TaskPriority;
import cn.bugstack.entityEnum.TaskStatus;
import cn.bugstack.exception.BusinessRuleViolationException;
import cn.bugstack.exception.DomainException;
import cn.bugstack.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final ConcurrentMap<UUID, Task> tasks = new ConcurrentHashMap<>();

    public record CreateTaskRequest(String title, String description, UUID projectId,
                                    UUID assigneeId, TaskPriority priority, LocalDateTime dueDate) {}
    public record UpdateTaskRequest(String title, String description, TaskPriority priority,
                                    LocalDateTime dueDate) {}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse create(@RequestBody CreateTaskRequest request) {
        if (request == null || request.projectId() == null || request.priority() == null) {
            throw new BusinessRuleViolationException("projectId and priority are required");
        }
        Task task = new Task(request.title(), request.description(), request.projectId(),
                request.assigneeId(), TaskStatus.TODO, request.priority(), request.dueDate());
        tasks.put(task.getId(), task);
        return TaskResponse.from(task);
    }

    @GetMapping
    public List<TaskResponse> list() {
        return tasks.values().stream().map(TaskResponse::from).toList();
    }

    @GetMapping("/{id}")
    public TaskResponse get(@PathVariable UUID id) {
        return TaskResponse.from(requireTask(id));
    }

    @PatchMapping("/{id}")
    public TaskResponse update(@PathVariable UUID id, @RequestBody UpdateTaskRequest request) {
        if (request == null) throw new BusinessRuleViolationException("Request body is required");
        Task task = requireTask(id);
        synchronized (task) {
            if (request.title() != null) task.changeTitle(request.title());
            if (request.description() != null) task.changeDescription(request.description());
            if (request.priority() != null) task.changePriority(request.priority());
            if (request.dueDate() != null) task.changeDueDate(request.dueDate());
            return TaskResponse.from(task);
        }
    }

    @PostMapping("/{id}/start")
    public TaskResponse start(@PathVariable UUID id) {
        Task task = requireTask(id);
        synchronized (task) { task.start(); return TaskResponse.from(task); }
    }

    @PostMapping("/{id}/complete")
    public TaskResponse complete(@PathVariable UUID id) {
        Task task = requireTask(id);
        synchronized (task) { task.complete(); return TaskResponse.from(task); }
    }

    @PostMapping("/{id}/cancel")
    public TaskResponse cancel(@PathVariable UUID id) {
        Task task = requireTask(id);
        synchronized (task) { task.cancel(); return TaskResponse.from(task); }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        if (tasks.remove(id) == null) throw new ResourceNotFoundException("Task", id);
    }

    private Task requireTask(UUID id) {
        Task task = tasks.get(id);
        if (task == null) throw new ResourceNotFoundException("Task", id);
        return task;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String notFound(ResourceNotFoundException exception) { return exception.getMessage(); }

    @ExceptionHandler(DomainException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String badRequest(DomainException exception) { return exception.getMessage(); }

}
