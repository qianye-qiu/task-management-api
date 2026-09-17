package cn.bugstack.repository;

import cn.bugstack.entity.Task;

import java.util.List;
import java.util.UUID;

public interface TaskRepository extends Repository<UUID, Task> {

    List<Task> findAll();
}
