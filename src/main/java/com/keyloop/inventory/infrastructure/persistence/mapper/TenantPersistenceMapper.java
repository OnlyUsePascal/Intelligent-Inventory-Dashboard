package com.keyloop.inventory.infrastructure.persistence.mapper;

import com.keyloop.inventory.domain.model.Tenant;
import com.keyloop.inventory.infrastructure.persistence.entity.TenantJpaEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper between Tenant domain model and TenantJpaEntity.
 */
@Component
public class TenantPersistenceMapper {

    public Tenant toDomain(TenantJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return Tenant.builder()
                .id(entity.getId())
                .name(entity.getName())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public TenantJpaEntity toEntity(Tenant domain) {
        if (domain == null) {
            return null;
        }
        TenantJpaEntity entity = new TenantJpaEntity();
        entity.setId(domain.getId());
        entity.setName(domain.getName());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}
