package com.keyloop.inventory.application.dto.response;

import com.keyloop.inventory.domain.model.InventoryType;
import com.keyloop.inventory.domain.model.VehicleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Response DTO for vehicle data, includes computed aging stock fields.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleResponse {

    private UUID id;
    private String vin;
    private String licensePlate;
    private String make;
    private String model;
    private Integer year;
    private Integer mileage;
    private VehicleStatus status;
    private InventoryType inventoryType;
    private LocalDate receivedDate;
    private LocalDate availableForSaleDate;
    
    // Computed aging stock fields
    private boolean isAgingStock;
    private long daysInInventory;
    
    // Audit fields
    private Instant createdAt;
    private Instant updatedAt;
}
