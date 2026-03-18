package com.keyloop.inventory.domain.exception;

import com.keyloop.inventory.domain.model.VehicleStatus;

import java.util.UUID;

/**
 * Exception thrown when attempting an operation on a vehicle that is not available.
 */
public class VehicleNotAvailableException extends DomainException {

    private final UUID vehicleId;
    private final VehicleStatus currentStatus;

    public VehicleNotAvailableException(UUID vehicleId, VehicleStatus currentStatus) {
        super(String.format("Vehicle %s is not available for reservation. Current status: %s",
                vehicleId, currentStatus));
        this.vehicleId = vehicleId;
        this.currentStatus = currentStatus;
    }

    public UUID getVehicleId() {
        return vehicleId;
    }

    public VehicleStatus getCurrentStatus() {
        return currentStatus;
    }
}
