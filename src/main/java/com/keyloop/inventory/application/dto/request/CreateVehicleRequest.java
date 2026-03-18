package com.keyloop.inventory.application.dto.request;

import com.keyloop.inventory.domain.model.InventoryType;
import com.keyloop.inventory.domain.model.VehicleStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request DTO for creating a new vehicle.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateVehicleRequest {

    @NotBlank(message = "VIN is required")
    @Size(min = 17, max = 17, message = "VIN must be exactly 17 characters")
    private String vin;

    @Size(max = 20, message = "License plate must not exceed 20 characters")
    private String licensePlate;

    @NotBlank(message = "Make is required")
    @Size(max = 100, message = "Make must not exceed 100 characters")
    private String make;

    @NotBlank(message = "Model is required")
    @Size(max = 100, message = "Model must not exceed 100 characters")
    private String model;

    private Integer year;

    private Integer mileage;

    @NotNull(message = "Inventory type is required")
    private InventoryType inventoryType;

    private VehicleStatus status;

    private LocalDate receivedDate;

    private LocalDate availableForSaleDate;
}
