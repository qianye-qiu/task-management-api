package cn.bugstack.entity;

import cn.bugstack.entityEnum.UserStatus;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

public class User {

    private UUID id;

    private String name;

    private String email;

    private String password;

    private UserStatus status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.status = UserStatus.ACTIVE;
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
    }

    public void changeEmail(String email) {
        this.email = email;
    }

    public void changePassword(String password) {
        this.password = password;
    }

    public void disable() {
        this.status = UserStatus.DISABLED;
    }
}
