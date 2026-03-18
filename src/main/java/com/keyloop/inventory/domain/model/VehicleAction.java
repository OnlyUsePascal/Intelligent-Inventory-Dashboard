package com.keyloop.inventory.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain entity representing an action/insight logged against a vehicle.
 * Pure POJO - no framework dependencies.
 */
public class VehicleAction {

    private UUID id;
    private UUID vehicleId;
    private UUID employeeId;
    private String actionText;
    private Instant timestamp;
    private Instant createdAt;
    private Instant updatedAt;

    public VehicleAction() {
    }

    public VehicleAction(UUID id, UUID vehicleId, UUID employeeId, String actionText,
                         Instant timestamp, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.vehicleId = vehicleId;
        this.employeeId = employeeId;
        this.actionText = actionText;
        this.timestamp = timestamp;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(UUID vehicleId) {
        this.vehicleId = vehicleId;
    }

    public UUID getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(UUID employeeId) {
        this.employeeId = employeeId;
    }

    public String getActionText() {
        return actionText;
    }

    public void setActionText(String actionText) {
        this.actionText = actionText;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
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
        private UUID vehicleId;
        private UUID employeeId;
        private String actionText;
        private Instant timestamp;
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder vehicleId(UUID vehicleId) {
            this.vehicleId = vehicleId;
            return this;
        }

        public Builder employeeId(UUID employeeId) {
            this.employeeId = employeeId;
            return this;
        }

        public Builder actionText(String actionText) {
            this.actionText = actionText;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
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

        public VehicleAction build() {
            return new VehicleAction(id, vehicleId, employeeId, actionText,
                    timestamp, createdAt, updatedAt);
        }
    }
}
