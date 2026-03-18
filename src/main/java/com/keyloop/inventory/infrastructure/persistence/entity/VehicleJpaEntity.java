package com.keyloop.inventory.infrastructure.persistence.entity;

import com.keyloop.inventory.infrastructure.persistence.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.UUID;

/**
 * JPA entity for Vehicle.
 */
@Entity
@Table(name = "vehicle")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class VehicleJpaEntity extends BaseEntity {

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "vin", nullable = false, length = 17)
    private String vin;

    @Column(name = "license_plate", length = 20)
    private String licensePlate;

    @Column(name = "make", nullable = false, length = 100)
    private String make;

    @Column(name = "model", nullable = false, length = 100)
    private String model;

    @Column(name = "year")
    private Integer year;

    @Column(name = "mileage")
    private Integer mileage;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "inventory_type", nullable = false, length = 50)
    private String inventoryType;

    @Column(name = "received_date")
    private LocalDate receivedDate;

    @Column(name = "available_for_sale_date")
    private LocalDate availableForSaleDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", insertable = false, updatable = false)
    private TenantJpaEntity tenant;
}
