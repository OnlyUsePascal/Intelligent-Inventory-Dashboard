package com.keyloop.inventory.infrastructure.persistence.repository;

import com.keyloop.inventory.infrastructure.persistence.entity.TenantJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Spring Data JPA repository for TenantJpaEntity.
 */
@Repository
public interface SpringDataTenantRepository extends JpaRepository<TenantJpaEntity, UUID> {
}
