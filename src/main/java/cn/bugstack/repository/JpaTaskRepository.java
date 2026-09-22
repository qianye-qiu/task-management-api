package cn.bugstack.repository;

import cn.bugstack.entity.Task;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
@Transactional(readOnly = true)
public class JpaTaskRepository implements TaskRepository {

    private final SpringDataTaskRepository delegate;

    public JpaTaskRepository(SpringDataTaskRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public List<Task> findAll() {
        return delegate.findAll();
    }

    @Override
    public Optional<Task> findById(UUID id) {
        return delegate.findById(id);
    }

    @Override
    @Transactional
    public void save(Task task) {
        delegate.save(task);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        delegate.deleteById(id);
    }
}
