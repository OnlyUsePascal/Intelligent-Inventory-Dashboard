package com.keyloop.inventory.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain entity representing a vehicle reservation.
 * Pure POJO - no framework dependencies.
 */
public class Reservation {

    private UUID id;
    private UUID vehicleId;
    private UUID employeeId;
    private Instant reservationDate;
    private Instant reservedUntilDate;
    private ReservationStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    public Reservation() {
    }

    public Reservation(UUID id, UUID vehicleId, UUID employeeId,
                       Instant reservationDate, Instant reservedUntilDate,
                       ReservationStatus status,
                       Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.vehicleId = vehicleId;
        this.employeeId = employeeId;
        this.reservationDate = reservationDate;
        this.reservedUntilDate = reservedUntilDate;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Business method: Check if reservation is still active.
     * Active means status is ACTIVE and hasn't passed reservedUntilDate.
     */
    public boolean isActive() {
        return status == ReservationStatus.ACTIVE 
                && reservedUntilDate != null 
                && reservedUntilDate.isAfter(Instant.now());
    }

    /**
     * Business method: Check if reservation has expired (based on time).
     */
    public boolean isExpired() {
        return reservedUntilDate != null && reservedUntilDate.isBefore(Instant.now());
    }

    /**
     * Cancel this reservation.
     */
    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
    }

    /**
     * Mark this reservation as expired.
     */
    public void expire() {
        this.status = ReservationStatus.EXPIRED;
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

    public Instant getReservationDate() {
        return reservationDate;
    }

    public void setReservationDate(Instant reservationDate) {
        this.reservationDate = reservationDate;
    }

    public Instant getReservedUntilDate() {
        return reservedUntilDate;
    }

    public void setReservedUntilDate(Instant reservedUntilDate) {
        this.reservedUntilDate = reservedUntilDate;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
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
        private Instant reservationDate;
        private Instant reservedUntilDate;
        private ReservationStatus status = ReservationStatus.ACTIVE;
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

        public Builder reservationDate(Instant reservationDate) {
            this.reservationDate = reservationDate;
            return this;
        }

        public Builder reservedUntilDate(Instant reservedUntilDate) {
            this.reservedUntilDate = reservedUntilDate;
            return this;
        }

        public Builder status(ReservationStatus status) {
            this.status = status;
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

        public Reservation build() {
            return new Reservation(id, vehicleId, employeeId, reservationDate,
                    reservedUntilDate, status, createdAt, updatedAt);
        }
    }
}
