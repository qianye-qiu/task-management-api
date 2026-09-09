package cn.bugstack.entity;

import cn.bugstack.entityEnum.ProjectStatus;

import java.time.LocalDateTime;
import java.util.UUID;

//任务所属项目
public class Project {

    private UUID id;

    private String name;

    private String description;

    private UUID ownerId;

    private ProjectStatus status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public Project(String name, String description, UUID ownerId, ProjectStatus status) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.ownerId = ownerId;
        this.status = status;
        this.createTime = LocalDateTime.now();
        this.updateTime = this.createTime;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void changeName(String name) {
        this.name = name;
    }

    public void changeDescription(String description) {
        this.description = description;
    }

    public void changeOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public void archived() {
        this.status = ProjectStatus.ARCHIVED;
    }
}
