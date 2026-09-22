package cn.bugstack.repository;

import cn.bugstack.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataTaskRepository extends JpaRepository<Task, UUID> {
}
