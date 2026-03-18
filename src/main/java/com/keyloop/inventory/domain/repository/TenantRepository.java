package com.keyloop.inventory.domain.repository;

import com.keyloop.inventory.domain.model.Tenant;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface (port) for Tenant entity.
 * Implementations are provided in the infrastructure layer.
 */
public interface TenantRepository {

    Optional<Tenant> findById(UUID id);

    Tenant save(Tenant tenant);

    void deleteById(UUID id);

    boolean existsById(UUID id);
}
