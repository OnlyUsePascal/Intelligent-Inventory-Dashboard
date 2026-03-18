package com.keyloop.inventory.infrastructure.persistence.mapper;

import com.keyloop.inventory.domain.model.VehicleAction;
import com.keyloop.inventory.infrastructure.persistence.entity.VehicleActionJpaEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper between VehicleAction domain model and VehicleActionJpaEntity.
 */
@Component
public class VehicleActionPersistenceMapper {

    public VehicleAction toDomain(VehicleActionJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return VehicleAction.builder()
                .id(entity.getId())
                .vehicleId(entity.getVehicleId())
                .employeeId(entity.getEmployeeId())
                .actionText(entity.getActionText())
                .timestamp(entity.getTimestamp())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public VehicleActionJpaEntity toEntity(VehicleAction domain) {
        if (domain == null) {
            return null;
        }
        VehicleActionJpaEntity entity = new VehicleActionJpaEntity();
        entity.setId(domain.getId());
        entity.setVehicleId(domain.getVehicleId());
        entity.setEmployeeId(domain.getEmployeeId());
        entity.setActionText(domain.getActionText());
        entity.setTimestamp(domain.getTimestamp());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}
