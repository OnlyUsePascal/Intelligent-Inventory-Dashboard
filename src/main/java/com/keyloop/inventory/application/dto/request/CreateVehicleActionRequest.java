package com.keyloop.inventory.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a vehicle action (insight).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateVehicleActionRequest {

    @NotBlank(message = "Action text is required")
    @Size(max = 5000, message = "Action text must not exceed 5000 characters")
    private String actionText;
}
