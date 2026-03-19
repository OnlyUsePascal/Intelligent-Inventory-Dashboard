package com.keyloop.inventory.domain.repository;

import com.keyloop.inventory.domain.model.Reservation;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface (port) for Reservation entity.
 * Implementations are provided in the infrastructure layer.
 */
public interface ReservationRepository {

    Optional<Reservation> findById(UUID id);

    List<Reservation> findByVehicleId(UUID vehicleId);

    List<Reservation> findActiveByVehicleId(UUID vehicleId);

    Optional<Reservation> findActiveByVehicleIdAndEmployeeId(UUID vehicleId, UUID employeeId);

    Reservation save(Reservation reservation);

    void deleteById(UUID id);

    boolean existsById(UUID id);

    boolean hasActiveReservation(UUID vehicleId);

    /**
     * Find all active reservations that should be expired.
     */
    List<Reservation> findExpiredReservations(Instant expiryTime);

    /**
     * Bulk expire reservations.
     * @return number of reservations expired
     */
    int expireReservations(Instant expiryTime);
}
