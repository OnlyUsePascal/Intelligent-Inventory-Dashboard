package com.keyloop.inventory.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for vehicle action data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleActionResponse {

    private UUID id;
    private UUID vehicleId;
    private UUID employeeId;
    private String actionText;
    private Instant timestamp;
    private Instant createdAt;
    private Instant updatedAt;
}
