package com.keyloop.inventory.application.mapper;

import com.keyloop.inventory.application.dto.request.CreateVehicleActionRequest;
import com.keyloop.inventory.application.dto.response.VehicleActionResponse;
import com.keyloop.inventory.domain.model.VehicleAction;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Mapper between VehicleAction domain model and DTOs.
 */
@Component
public class VehicleActionMapper {

    /**
     * Convert domain VehicleAction to VehicleActionResponse DTO.
     */
    public VehicleActionResponse toResponse(VehicleAction vehicleAction) {
        if (vehicleAction == null) {
            return null;
        }

        return VehicleActionResponse.builder()
                .id(vehicleAction.getId())
                .vehicleId(vehicleAction.getVehicleId())
                .employeeId(vehicleAction.getEmployeeId())
                .actionText(vehicleAction.getActionText())
                .timestamp(vehicleAction.getTimestamp())
                .createdAt(vehicleAction.getCreatedAt())
                .updatedAt(vehicleAction.getUpdatedAt())
                .build();
    }

    /**
     * Convert CreateVehicleActionRequest to domain VehicleAction.
     */
    public VehicleAction toEntity(CreateVehicleActionRequest request, UUID vehicleId, UUID employeeId) {
        if (request == null) {
            return null;
        }

        return VehicleAction.builder()
                .vehicleId(vehicleId)
                .employeeId(employeeId)
                .actionText(request.getActionText())
                .timestamp(Instant.now())
                .build();
    }
}
