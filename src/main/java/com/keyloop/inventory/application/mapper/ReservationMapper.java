package com.keyloop.inventory.application.mapper;

import com.keyloop.inventory.application.dto.request.CreateReservationRequest;
import com.keyloop.inventory.application.dto.response.ReservationResponse;
import com.keyloop.inventory.domain.model.Reservation;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Mapper between Reservation domain model and DTOs.
 */
@Component
public class ReservationMapper {

    /**
     * Convert domain Reservation to ReservationResponse DTO.
     */
    public ReservationResponse toResponse(Reservation reservation) {
        if (reservation == null) {
            return null;
        }

        return ReservationResponse.builder()
                .id(reservation.getId())
                .vehicleId(reservation.getVehicleId())
                .employeeId(reservation.getEmployeeId())
                .reservationDate(reservation.getReservationDate())
                .reservedUntilDate(reservation.getReservedUntilDate())
                .active(reservation.isActive())
                .createdAt(reservation.getCreatedAt())
                .updatedAt(reservation.getUpdatedAt())
                .build();
    }

    /**
     * Convert CreateReservationRequest to domain Reservation.
     */
    public Reservation toEntity(CreateReservationRequest request, UUID vehicleId, UUID employeeId) {
        if (request == null) {
            return null;
        }

        return Reservation.builder()
                .vehicleId(vehicleId)
                .employeeId(employeeId)
                .reservationDate(Instant.now())
                .reservedUntilDate(request.getReservedUntilDate())
                .build();
    }
}
