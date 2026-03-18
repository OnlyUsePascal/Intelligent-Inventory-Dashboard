package com.keyloop.inventory.application.dto.request;

import com.keyloop.inventory.domain.model.InventoryType;
import com.keyloop.inventory.domain.model.VehicleStatus;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request DTO for updating an existing vehicle.
 * All fields are optional - only provided fields will be updated.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateVehicleRequest {

    @Size(max = 20, message = "License plate must not exceed 20 characters")
    private String licensePlate;

    @Size(max = 100, message = "Make must not exceed 100 characters")
    private String make;

    @Size(max = 100, message = "Model must not exceed 100 characters")
    private String model;

    private Integer year;

    private Integer mileage;

    private VehicleStatus status;

    private InventoryType inventoryType;

    private LocalDate receivedDate;

    private LocalDate availableForSaleDate;
}
