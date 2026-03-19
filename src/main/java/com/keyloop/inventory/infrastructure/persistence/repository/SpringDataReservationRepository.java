package com.keyloop.inventory.infrastructure.persistence.repository;

import com.keyloop.inventory.domain.model.ReservationStatus;
import com.keyloop.inventory.infrastructure.persistence.entity.ReservationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for ReservationJpaEntity.
 */
@Repository
public interface SpringDataReservationRepository extends JpaRepository<ReservationJpaEntity, UUID> {

    List<ReservationJpaEntity> findByVehicleIdOrderByReservationDateDesc(UUID vehicleId);

    @Query("SELECT r FROM ReservationJpaEntity r WHERE r.vehicleId = :vehicleId AND r.status = 'ACTIVE'")
    List<ReservationJpaEntity> findActiveByVehicleId(@Param("vehicleId") UUID vehicleId);

    @Query("SELECT r FROM ReservationJpaEntity r WHERE r.vehicleId = :vehicleId AND r.employeeId = :employeeId AND r.status = 'ACTIVE'")
    Optional<ReservationJpaEntity> findActiveByVehicleIdAndEmployeeId(@Param("vehicleId") UUID vehicleId, 
                                                                       @Param("employeeId") UUID employeeId);

    @Query("SELECT COUNT(r) > 0 FROM ReservationJpaEntity r WHERE r.vehicleId = :vehicleId AND r.status = 'ACTIVE'")
    boolean hasActiveReservation(@Param("vehicleId") UUID vehicleId);

    /**
     * Find all active reservations that have exceeded the expiry date.
     * Used by the scheduled expiry job.
     */
    @Query("SELECT r FROM ReservationJpaEntity r WHERE r.status = 'ACTIVE' AND r.reservedUntilDate < :expiryTime")
    List<ReservationJpaEntity> findExpiredReservations(@Param("expiryTime") Instant expiryTime);

    /**
     * Bulk update expired reservations to EXPIRED status.
     */
    @Modifying
    @Query("UPDATE ReservationJpaEntity r SET r.status = :newStatus WHERE r.status = 'ACTIVE' AND r.reservedUntilDate < :expiryTime")
    int expireReservations(@Param("expiryTime") Instant expiryTime, @Param("newStatus") ReservationStatus newStatus);
}
