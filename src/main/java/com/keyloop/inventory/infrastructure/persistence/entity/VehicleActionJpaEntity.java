package com.keyloop.inventory.infrastructure.persistence.entity;

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
 * JPA entity for VehicleAction.
 */
@Entity
@Table(name = "vehicle_action")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class VehicleActionJpaEntity extends BaseEntity {

    @Column(name = "vehicle_id", nullable = false)
    private UUID vehicleId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "action_text", nullable = false, columnDefinition = "TEXT")
    private String actionText;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", insertable = false, updatable = false)
    private VehicleJpaEntity vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", insertable = false, updatable = false)
    private EmployeeJpaEntity employee;
}
