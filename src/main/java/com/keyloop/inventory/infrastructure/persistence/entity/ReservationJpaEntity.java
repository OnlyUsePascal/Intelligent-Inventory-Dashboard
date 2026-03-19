package com.keyloop.inventory.infrastructure.persistence.entity;

import com.keyloop.inventory.domain.model.ReservationStatus;
import com.keyloop.inventory.infrastructure.persistence.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for Reservation.
 */
@Entity
@Table(name = "reservation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ReservationJpaEntity extends BaseEntity {

    @Column(name = "vehicle_id", nullable = false)
    private UUID vehicleId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "reservation_date", nullable = false)
    private Instant reservationDate;

    @Column(name = "reserved_until_date", nullable = false)
    private Instant reservedUntilDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReservationStatus status = ReservationStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", insertable = false, updatable = false)
    private VehicleJpaEntity vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", insertable = false, updatable = false)
    private EmployeeJpaEntity employee;
}
