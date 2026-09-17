package cn.bugstack.repository;

import cn.bugstack.entity.Task;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class InMemoryTaskRepository implements TaskRepository {

    private final ConcurrentMap<UUID, Task> storage = new ConcurrentHashMap<>();

    @Override
    public List<Task> findAll() {
        return List.copyOf(storage.values());
    }

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
