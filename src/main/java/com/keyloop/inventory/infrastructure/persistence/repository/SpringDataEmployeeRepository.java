package com.keyloop.inventory.infrastructure.persistence.repository;

import com.keyloop.inventory.infrastructure.persistence.entity.EmployeeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for EmployeeJpaEntity.
 */
@Repository
public interface SpringDataEmployeeRepository extends JpaRepository<EmployeeJpaEntity, UUID> {

    Optional<EmployeeJpaEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    List<EmployeeJpaEntity> findByTenantId(UUID tenantId);

    boolean existsByIdAndTenantId(UUID id, UUID tenantId);
}
