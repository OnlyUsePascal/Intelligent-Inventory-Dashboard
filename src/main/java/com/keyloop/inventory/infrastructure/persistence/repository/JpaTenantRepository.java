package com.keyloop.inventory.infrastructure.persistence.repository;

import com.keyloop.inventory.domain.model.Tenant;
import com.keyloop.inventory.domain.repository.TenantRepository;
import com.keyloop.inventory.infrastructure.persistence.mapper.TenantPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implementation of TenantRepository using Spring Data JPA.
 */
@Repository
@RequiredArgsConstructor
public class JpaTenantRepository implements TenantRepository {

    private final SpringDataTenantRepository springDataRepository;
    private final TenantPersistenceMapper mapper;

    @Override
    public Optional<Tenant> findById(UUID id) {
        return springDataRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Tenant save(Tenant tenant) {
        var entity = mapper.toEntity(tenant);
        var saved = springDataRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public void deleteById(UUID id) {
        springDataRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return springDataRepository.existsById(id);
    }
}
