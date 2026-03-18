package com.keyloop.inventory.infrastructure.persistence.mapper;

import com.keyloop.inventory.domain.model.InventoryType;
import com.keyloop.inventory.domain.model.Vehicle;
import com.keyloop.inventory.domain.model.VehicleStatus;
import com.keyloop.inventory.infrastructure.persistence.entity.VehicleJpaEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper between Vehicle domain model and VehicleJpaEntity.
 */
@Component
public class VehiclePersistenceMapper {

    public Vehicle toDomain(VehicleJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return Vehicle.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .vin(entity.getVin())
                .licensePlate(entity.getLicensePlate())
                .make(entity.getMake())
                .model(entity.getModel())
                .year(entity.getYear())
                .mileage(entity.getMileage())
                .status(VehicleStatus.valueOf(entity.getStatus()))
                .inventoryType(InventoryType.valueOf(entity.getInventoryType()))
                .receivedDate(entity.getReceivedDate())
                .availableForSaleDate(entity.getAvailableForSaleDate())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public VehicleJpaEntity toEntity(Vehicle domain) {
        if (domain == null) {
            return null;
        }
        VehicleJpaEntity entity = new VehicleJpaEntity();
        entity.setId(domain.getId());
        entity.setTenantId(domain.getTenantId());
        entity.setVin(domain.getVin());
        entity.setLicensePlate(domain.getLicensePlate());
        entity.setMake(domain.getMake());
        entity.setModel(domain.getModel());
        entity.setYear(domain.getYear());
        entity.setMileage(domain.getMileage());
        entity.setStatus(domain.getStatus().name());
        entity.setInventoryType(domain.getInventoryType().name());
        entity.setReceivedDate(domain.getReceivedDate());
        entity.setAvailableForSaleDate(domain.getAvailableForSaleDate());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}
