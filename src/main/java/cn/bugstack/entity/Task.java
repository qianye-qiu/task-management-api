package cn.bugstack.entity;

import cn.bugstack.entityEnum.TaskPriority;
import cn.bugstack.entityEnum.TaskStatus;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

public class Task {

    private UUID id;

    private String title;

    private String description;

    private TaskStatus status;

    private TaskPriority priority;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private LocalDateTime dueDate;

    public Task(String title, String description, TaskStatus status,
                TaskPriority priority, LocalDateTime dueDate) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.dueDate = dueDate;
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

    public TaskStatus getStatus() {
        return status;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void changeTitle(String title) {
        this.title = title;
    }

    public void changeDescription(String description) {
        this.description = description;
    }

    public void start() {
        if(status != TaskStatus.TODO){
            throw new IllegalStateException("Only TODO task can be started");
        }
        this.status = TaskStatus.IN_PROGRESS;
    }

    public void complete(){
        if(status != TaskStatus.IN_PROGRESS){
            throw new IllegalStateException("Only IN_PROGRESS task can be completed");
        }
        this.status = TaskStatus.COMPLETED;
    }

    public void cancel(){
        if(status == TaskStatus.COMPLETED){
            throw new IllegalStateException("COMPLETED task cannot be canceled");
        }
        this.status = TaskStatus.CANCELLED;
    }

    public void changePriority(TaskPriority priority) {
        this.priority = priority;
    }

    public void changeDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }
}
