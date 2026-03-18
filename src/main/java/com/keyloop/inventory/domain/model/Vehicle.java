package com.keyloop.inventory.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Domain entity representing a vehicle in the inventory.
 * Pure POJO - no framework dependencies.
 */
public class Vehicle {

    private UUID id;
    private UUID tenantId;
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
    private Instant createdAt;
    private Instant updatedAt;

    public Vehicle() {
    }

    public Vehicle(UUID id, UUID tenantId, String vin, String licensePlate,
                   String make, String model, Integer year, Integer mileage,
                   VehicleStatus status, InventoryType inventoryType,
                   LocalDate receivedDate, LocalDate availableForSaleDate,
                   Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.vin = vin;
        this.licensePlate = licensePlate;
        this.make = make;
        this.model = model;
        this.year = year;
        this.mileage = mileage;
        this.status = status;
        this.inventoryType = inventoryType;
        this.receivedDate = receivedDate;
        this.availableForSaleDate = availableForSaleDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Business method: Check if this vehicle can be reserved.
     */
    public boolean canBeReserved() {
        return status == VehicleStatus.AVAILABLE;
    }

    /**
     * Business method: Reserve this vehicle.
     */
    public void reserve() {
        if (!canBeReserved()) {
            throw new IllegalStateException("Vehicle cannot be reserved - current status: " + status);
        }
        this.status = VehicleStatus.RESERVED;
    }

    /**
     * Business method: Make vehicle available again.
     */
    public void makeAvailable() {
        this.status = VehicleStatus.AVAILABLE;
    }

    /**
     * Business method: Mark vehicle as sold.
     */
    public void markAsSold() {
        this.status = VehicleStatus.SOLD;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getMileage() {
        return mileage;
    }

    public void setMileage(Integer mileage) {
        this.mileage = mileage;
    }

    public VehicleStatus getStatus() {
        return status;
    }

    public void setStatus(VehicleStatus status) {
        this.status = status;
    }

    public InventoryType getInventoryType() {
        return inventoryType;
    }

    public void setInventoryType(InventoryType inventoryType) {
        this.inventoryType = inventoryType;
    }

    public LocalDate getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(LocalDate receivedDate) {
        this.receivedDate = receivedDate;
    }

    public LocalDate getAvailableForSaleDate() {
        return availableForSaleDate;
    }

    public void setAvailableForSaleDate(LocalDate availableForSaleDate) {
        this.availableForSaleDate = availableForSaleDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static class Builder {
        private UUID id;
        private UUID tenantId;
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
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder tenantId(UUID tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public Builder vin(String vin) {
            this.vin = vin;
            return this;
        }

        public Builder licensePlate(String licensePlate) {
            this.licensePlate = licensePlate;
            return this;
        }

        public Builder make(String make) {
            this.make = make;
            return this;
        }

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public Builder year(Integer year) {
            this.year = year;
            return this;
        }

        public Builder mileage(Integer mileage) {
            this.mileage = mileage;
            return this;
        }

        public Builder status(VehicleStatus status) {
            this.status = status;
            return this;
        }

        public Builder inventoryType(InventoryType inventoryType) {
            this.inventoryType = inventoryType;
            return this;
        }

        public Builder receivedDate(LocalDate receivedDate) {
            this.receivedDate = receivedDate;
            return this;
        }

        public Builder availableForSaleDate(LocalDate availableForSaleDate) {
            this.availableForSaleDate = availableForSaleDate;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Vehicle build() {
            return new Vehicle(id, tenantId, vin, licensePlate, make, model,
                    year, mileage, status, inventoryType, receivedDate,
                    availableForSaleDate, createdAt, updatedAt);
        }
    }
}
