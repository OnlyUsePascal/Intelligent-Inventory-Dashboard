package com.keyloop.inventory.infrastructure.persistence.mapper;

import com.keyloop.inventory.domain.model.Employee;
import com.keyloop.inventory.domain.model.EmployeeRole;
import com.keyloop.inventory.infrastructure.persistence.entity.EmployeeJpaEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper between Employee domain model and EmployeeJpaEntity.
 */
@Component
public class EmployeePersistenceMapper {

    public Employee toDomain(EmployeeJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return Employee.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .name(entity.getName())
                .role(EmployeeRole.valueOf(entity.getRole()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public EmployeeJpaEntity toEntity(Employee domain) {
        if (domain == null) {
            return null;
        }
        EmployeeJpaEntity entity = new EmployeeJpaEntity();
        entity.setId(domain.getId());
        entity.setTenantId(domain.getTenantId());
        entity.setName(domain.getName());
        entity.setRole(domain.getRole().name());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}
