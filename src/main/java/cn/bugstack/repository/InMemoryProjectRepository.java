package cn.bugstack.repository;

import cn.bugstack.entity.Project;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemoryProjectRepository implements Repository<UUID, Project> {

    private final Map<UUID, Project> storage = new HashMap<>();

    @Override
    public Optional<Project> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public void save(Project project) {
        if (project == null){
            throw new IllegalArgumentException("Project cannot be null");
        }

        if (project.getId() == null){
            throw new IllegalArgumentException("Project id cannot be null");
        }

        storage.put(project.getId(), project);
    }

    @Override
    public void delete(UUID id) {
        storage.remove(id);
    }
}
