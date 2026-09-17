package cn.bugstack.service;

import cn.bugstack.dto.CreateTaskRequest;
import cn.bugstack.dto.TaskResponse;
import cn.bugstack.dto.UpdateTaskRequest;
import cn.bugstack.entity.Task;
import cn.bugstack.entityEnum.TaskStatus;
import cn.bugstack.exception.BusinessRuleViolationException;
import cn.bugstack.exception.ResourceNotFoundException;
import cn.bugstack.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@Service
public class TaskService {

    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    public synchronized TaskResponse create(CreateTaskRequest request) {
        if (request == null || request.projectId() == null || request.priority() == null) {
            throw new BusinessRuleViolationException("projectId and priority are required");
        }
        Task task = new Task(request.title(), request.description(), request.projectId(),
                request.assigneeId(), TaskStatus.TODO, request.priority(), request.dueDate());
        repository.save(task);
        return TaskResponse.from(task);
    }

    public synchronized List<TaskResponse> list() {
        return repository.findAll().stream()
                .sorted(Comparator.comparing(Task::getCreatedAt).thenComparing(Task::getId))
                .map(TaskResponse::from)
                .toList();
    }

    public synchronized TaskResponse get(UUID id) {
        return TaskResponse.from(requireTask(id));
    }

    public synchronized TaskResponse update(UUID id, UpdateTaskRequest request) {
        if (request == null) throw new BusinessRuleViolationException("Request body is required");
        Task task = requireTask(id);
        // Title validation runs before any other changes so rejection leaves the task intact.
        if (request.title() != null) task.changeTitle(request.title());
        if (request.description() != null) task.changeDescription(request.description());
        if (request.priority() != null) task.changePriority(request.priority());
        if (request.dueDate() != null) task.changeDueDate(request.dueDate());
        repository.save(task);
        return TaskResponse.from(task);
    }

    public synchronized TaskResponse start(UUID id) {
        return changeState(id, Task::start);
    }

    public synchronized TaskResponse complete(UUID id) {
        return changeState(id, Task::complete);
    }

    public synchronized TaskResponse cancel(UUID id) {
        return changeState(id, Task::cancel);
    }

    public synchronized void delete(UUID id) {
        requireTask(id);
        repository.delete(id);
    }

    private TaskResponse changeState(UUID id, Consumer<Task> change) {
        Task task = requireTask(id);
        change.accept(task);
        repository.save(task);
        return TaskResponse.from(task);
    }

    private Task requireTask(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", id));
    }
}
