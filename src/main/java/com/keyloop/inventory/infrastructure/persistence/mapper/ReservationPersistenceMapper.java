package com.keyloop.inventory.infrastructure.persistence.mapper;

import com.keyloop.inventory.domain.model.Reservation;
import com.keyloop.inventory.infrastructure.persistence.entity.ReservationJpaEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper between Reservation domain model and ReservationJpaEntity.
 */
@Component
public class ReservationPersistenceMapper {

    public Reservation toDomain(ReservationJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return Reservation.builder()
                .id(entity.getId())
                .vehicleId(entity.getVehicleId())
                .employeeId(entity.getEmployeeId())
                .reservationDate(entity.getReservationDate())
                .reservedUntilDate(entity.getReservedUntilDate())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public ReservationJpaEntity toEntity(Reservation domain) {
        if (domain == null) {
            return null;
        }
        ReservationJpaEntity entity = new ReservationJpaEntity();
        entity.setId(domain.getId());
        entity.setVehicleId(domain.getVehicleId());
        entity.setEmployeeId(domain.getEmployeeId());
        entity.setReservationDate(domain.getReservationDate());
        entity.setReservedUntilDate(domain.getReservedUntilDate());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}
