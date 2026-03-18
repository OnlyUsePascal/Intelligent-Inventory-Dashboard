package com.keyloop.inventory.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for reservation data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponse {

    private UUID id;
    private UUID vehicleId;
    private UUID employeeId;
    private Instant reservationDate;
    private Instant reservedUntilDate;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
