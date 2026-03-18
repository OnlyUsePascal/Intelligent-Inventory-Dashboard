package com.keyloop.inventory.application.dto.response;

import com.keyloop.inventory.domain.model.EmployeeRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for employee data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse {

    private UUID id;
    private UUID tenantId;
    private String name;
    private EmployeeRole role;
    private Instant createdAt;
    private Instant updatedAt;
}
