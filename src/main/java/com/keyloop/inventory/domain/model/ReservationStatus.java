package com.keyloop.inventory.domain.model;

/**
 * Status of a vehicle reservation.
 */
public enum ReservationStatus {
    /**
     * Reservation is currently active.
     */
    ACTIVE,

    /**
     * Reservation was cancelled by the employee or admin.
     */
    CANCELLED,

    /**
     * Reservation expired (exceeded 5 days).
     */
    EXPIRED
}
