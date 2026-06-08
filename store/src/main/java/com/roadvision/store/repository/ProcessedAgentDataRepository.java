package com.roadvision.store.repository;

import com.roadvision.store.entity.ProcessedAgentDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Репозиторій для CRUD-операцій над таблицею processed_agent_data.
 * Базові операції (save, findById, findAll, deleteById, ...) надає Spring Data JPA.
 */
@Repository
public interface ProcessedAgentDataRepository extends JpaRepository<ProcessedAgentDataEntity, Long> {
}
