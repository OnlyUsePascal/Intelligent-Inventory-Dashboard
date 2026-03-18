package com.keyloop.inventory.infrastructure.persistence.repository;

import com.keyloop.inventory.infrastructure.persistence.entity.ReservationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for ReservationJpaEntity.
 */
@Repository
public interface SpringDataReservationRepository extends JpaRepository<ReservationJpaEntity, UUID> {

    List<ReservationJpaEntity> findByVehicleId(UUID vehicleId);

    @Query("SELECT r FROM ReservationJpaEntity r WHERE r.vehicleId = :vehicleId AND r.reservedUntilDate > CURRENT_TIMESTAMP")
    List<ReservationJpaEntity> findActiveByVehicleId(@Param("vehicleId") UUID vehicleId);

    @Query("SELECT r FROM ReservationJpaEntity r WHERE r.vehicleId = :vehicleId AND r.employeeId = :employeeId AND r.reservedUntilDate > CURRENT_TIMESTAMP")
    Optional<ReservationJpaEntity> findActiveByVehicleIdAndEmployeeId(@Param("vehicleId") UUID vehicleId, 
                                                                       @Param("employeeId") UUID employeeId);

    @Query("SELECT COUNT(r) > 0 FROM ReservationJpaEntity r WHERE r.vehicleId = :vehicleId AND r.reservedUntilDate > CURRENT_TIMESTAMP")
    boolean hasActiveReservation(@Param("vehicleId") UUID vehicleId);
}
