package cn.bugstack.entity;

import cn.bugstack.entityEnum.TaskPriority;
import cn.bugstack.entityEnum.TaskStatus;
import cn.bugstack.exception.BusinessRuleViolationException;
import cn.bugstack.exception.InvalidStateException;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

//任务
public class Task {

    private UUID id;

    private String title;

    private String description;

    private UUID projectId;

    private UUID assigneeId;

    private TaskStatus status;

    private TaskPriority priority;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime dueDate;

    private LocalDateTime completedAt;

    public Task(String title, String description,
                UUID projectId, UUID assigneeId, TaskStatus status,
                TaskPriority priority, LocalDateTime dueDate) {
        this.id = UUID.randomUUID();
        validateTitle(title);
        this.title = title;
        this.description = description;
        this.projectId = projectId;
        this.assigneeId = assigneeId;
        this.status = status;
        this.priority = priority;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        this.dueDate = dueDate;
        if(status == TaskStatus.COMPLETED) {
            this.completedAt = createdAt;
        }
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public UUID getAssigneeId() {
        return assigneeId;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void changeTitle(String title) {
        validateTitle(title);
        this.title = title;
        this.updatedAt = LocalDateTime.now();
    }

    public void changeDescription(String description) {
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }

    public void start() {
        if(status != TaskStatus.TODO){
            throw new InvalidStateException("Only TODO task can be started");
        }
        this.status = TaskStatus.IN_PROGRESS;
        this.updatedAt = LocalDateTime.now();
    }

    public void complete(){
        if(status != TaskStatus.IN_PROGRESS){
            throw new InvalidStateException("Only IN_PROGRESS task can be completed");
        }
        LocalDateTime now = LocalDateTime.now();
        this.status = TaskStatus.COMPLETED;
        this.completedAt = now;
        this.updatedAt = now;
    }

    public void cancel(){
        if(status == TaskStatus.COMPLETED){
            throw new InvalidStateException("COMPLETED task cannot be canceled");
        }
        this.status = TaskStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    public void changePriority(TaskPriority priority) {
        this.priority = priority;
        this.updatedAt = LocalDateTime.now();
    }

    public void changeDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
        this.updatedAt = LocalDateTime.now();
    }

    private static void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new BusinessRuleViolationException("Task title cannot be blank");
        }
    }
}
