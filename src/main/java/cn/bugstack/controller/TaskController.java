package cn.bugstack.controller;

import cn.bugstack.dto.CreateTaskRequest;
import cn.bugstack.dto.TaskResponse;
import cn.bugstack.dto.UpdateTaskRequest;
import cn.bugstack.entity.Task;
import cn.bugstack.entityEnum.TaskPriority;
import cn.bugstack.entityEnum.TaskStatus;
import cn.bugstack.exception.BusinessRuleViolationException;
import cn.bugstack.exception.DomainException;
import cn.bugstack.exception.ResourceNotFoundException;
import cn.bugstack.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody CreateTaskRequest request) {
        TaskResponse task = service.create(request);
        return ResponseEntity.created(URI.create("/api/tasks/" + task.id())).body(task);
    }

    @GetMapping
    public List<TaskResponse> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public TaskResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PatchMapping("/{id}")
    public TaskResponse update(@PathVariable UUID id, @RequestBody UpdateTaskRequest request) {
        return service.update(id, request);
    }

    @PostMapping("/{id}/start")
    public TaskResponse start(@PathVariable UUID id) {
        return service.start(id);
    }

    @PostMapping("/{id}/complete")
    public TaskResponse complete(@PathVariable UUID id) {
        return service.complete(id);
    }

    @PostMapping("/{id}/cancel")
    public TaskResponse cancel(@PathVariable UUID id) {
        return service.cancel(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }

}
