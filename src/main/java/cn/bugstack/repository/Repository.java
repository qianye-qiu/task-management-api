package cn.bugstack.repository;

import java.util.Optional;

public interface Repository<ID, T> {

    Optional<T> findById(ID id);

    void save(T entity);

    void delete(ID id);
}
