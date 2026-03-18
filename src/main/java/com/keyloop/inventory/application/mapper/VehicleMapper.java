package com.keyloop.inventory.application.mapper;

import com.keyloop.inventory.application.dto.request.CreateVehicleRequest;
import com.keyloop.inventory.application.dto.request.UpdateVehicleRequest;
import com.keyloop.inventory.application.dto.response.VehicleResponse;
import com.keyloop.inventory.domain.model.Vehicle;
import com.keyloop.inventory.domain.model.VehicleStatus;
import com.keyloop.inventory.domain.service.AgingStockCalculator;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Mapper between Vehicle domain model and DTOs.
 */
@Component
public class VehicleMapper {

    private final AgingStockCalculator agingStockCalculator;

    public VehicleMapper() {
        this.agingStockCalculator = new AgingStockCalculator();
    }

    /**
     * Convert domain Vehicle to VehicleResponse DTO with calculated aging stock fields.
     */
    public VehicleResponse toResponse(Vehicle vehicle) {
        if (vehicle == null) {
            return null;
        }

        long daysInInventory = agingStockCalculator.calculateDaysInInventory(vehicle);
        boolean isAgingStock = agingStockCalculator.isAgingStock(vehicle);

        return VehicleResponse.builder()
                .id(vehicle.getId())
                .vin(vehicle.getVin())
                .licensePlate(vehicle.getLicensePlate())
                .make(vehicle.getMake())
                .model(vehicle.getModel())
                .year(vehicle.getYear())
                .mileage(vehicle.getMileage())
                .status(vehicle.getStatus())
                .inventoryType(vehicle.getInventoryType())
                .receivedDate(vehicle.getReceivedDate())
                .availableForSaleDate(vehicle.getAvailableForSaleDate())
                .isAgingStock(isAgingStock)
                .daysInInventory(daysInInventory)
                .createdAt(vehicle.getCreatedAt())
                .updatedAt(vehicle.getUpdatedAt())
                .build();
    }

    /**
     * Convert CreateVehicleRequest to domain Vehicle (for new vehicle creation).
     */
    public Vehicle toEntity(CreateVehicleRequest request, UUID tenantId) {
        if (request == null) {
            return null;
        }

        return Vehicle.builder()
                .tenantId(tenantId)
                .vin(request.getVin())
                .licensePlate(request.getLicensePlate())
                .make(request.getMake())
                .model(request.getModel())
                .year(request.getYear())
                .mileage(request.getMileage())
                .status(request.getStatus() != null ? request.getStatus() : VehicleStatus.AVAILABLE)
                .inventoryType(request.getInventoryType())
                .receivedDate(request.getReceivedDate())
                .availableForSaleDate(request.getAvailableForSaleDate())
                .build();
    }

    /**
     * Apply UpdateVehicleRequest fields to an existing Vehicle.
     * Only non-null fields in the request will update the vehicle.
     */
    public void updateEntity(Vehicle vehicle, UpdateVehicleRequest request) {
        if (vehicle == null || request == null) {
            return;
        }

        if (request.getLicensePlate() != null) {
            vehicle.setLicensePlate(request.getLicensePlate());
        }
        if (request.getMake() != null) {
            vehicle.setMake(request.getMake());
        }
        if (request.getModel() != null) {
            vehicle.setModel(request.getModel());
        }
        if (request.getYear() != null) {
            vehicle.setYear(request.getYear());
        }
        if (request.getMileage() != null) {
            vehicle.setMileage(request.getMileage());
        }
        if (request.getStatus() != null) {
            vehicle.setStatus(request.getStatus());
        }
        if (request.getInventoryType() != null) {
            vehicle.setInventoryType(request.getInventoryType());
        }
        if (request.getReceivedDate() != null) {
            vehicle.setReceivedDate(request.getReceivedDate());
        }
        if (request.getAvailableForSaleDate() != null) {
            vehicle.setAvailableForSaleDate(request.getAvailableForSaleDate());
        }
    }
}
