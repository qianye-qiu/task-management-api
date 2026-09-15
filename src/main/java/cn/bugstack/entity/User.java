package cn.bugstack.entity;

import cn.bugstack.entityEnum.UserStatus;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

//用户
public class User {

    private UUID id;

    private String name;

    private String email;

    private String password;

    private UserStatus status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public User(String name, String email, String password) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.email = email;
        this.password = password;
        this.status = UserStatus.ACTIVE;
        this.createTime = LocalDateTime.now();
        this.updateTime = this.createTime;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public UserStatus getStatus() {
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
        this.updateTime = LocalDateTime.now();
    }

    public void changeEmail(String email) {
        this.email = email;
        this.updateTime = LocalDateTime.now();
    }

    public void changePassword(String password) {
        this.password = password;
        this.updateTime = LocalDateTime.now();
    }

    public void disable() {
        this.status = UserStatus.DISABLED;
        this.updateTime = LocalDateTime.now();
    }
}
