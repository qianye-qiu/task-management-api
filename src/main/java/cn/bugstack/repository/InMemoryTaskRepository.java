package cn.bugstack.repository;

import cn.bugstack.entity.Task;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemoryTaskRepository implements Repository<UUID, Task> {

    private final Map<UUID, Task> storage = new HashMap<>();

    @Override
    public Optional<Task> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public void save(Task task) {
        if(task == null){
            throw new IllegalArgumentException("Task cannot be null");
        }

        if(task.getId() == null){
            throw new IllegalArgumentException("Task id cannot be null");
        }

        storage.put(task.getId(), task);
    }

    @Override
    public void delete(UUID id) {
        storage.remove(id);
    }
}
