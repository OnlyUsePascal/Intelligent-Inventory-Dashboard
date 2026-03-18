package com.keyloop.inventory.infrastructure.web.controller;

import com.keyloop.inventory.application.dto.request.CreateReservationRequest;
import com.keyloop.inventory.application.dto.request.CreateVehicleActionRequest;
import com.keyloop.inventory.application.dto.request.CreateVehicleRequest;
import com.keyloop.inventory.application.dto.request.UpdateVehicleRequest;
import com.keyloop.inventory.application.dto.response.ReservationResponse;
import com.keyloop.inventory.application.dto.response.VehicleActionResponse;
import com.keyloop.inventory.application.dto.response.VehicleResponse;
import com.keyloop.inventory.application.usecase.ReservationService;
import com.keyloop.inventory.application.usecase.VehicleActionService;
import com.keyloop.inventory.application.usecase.VehicleService;
import com.keyloop.inventory.domain.model.InventoryType;
import com.keyloop.inventory.domain.model.VehicleStatus;
import com.keyloop.inventory.domain.repository.VehicleRepository;
import com.keyloop.inventory.infrastructure.web.dto.ApiResponse;
import com.keyloop.inventory.infrastructure.web.dto.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for vehicle management endpoints.
 * Base path: /api/v1/inventory-vehicles
 */
@RestController
@RequestMapping("/api/v1/inventory-vehicles")
@RequiredArgsConstructor
@Tag(name = "Vehicles", description = "Vehicle inventory management API")
public class VehicleController {

    private final VehicleService vehicleService;
    private final ReservationService reservationService;
    private final VehicleActionService vehicleActionService;

    // ==================== Vehicle Endpoints ====================

    @GetMapping
    @Operation(summary = "List vehicles", description = "Get paginated list of vehicles with optional filters")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved vehicles"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request parameters")
    })
    public ResponseEntity<PagedResponse<VehicleResponse>> listVehicles(
            @Parameter(description = "Tenant ID", required = true)
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @Parameter(description = "Filter by make")
            @RequestParam(required = false) String make,
            @Parameter(description = "Filter by model")
            @RequestParam(required = false) String model,
            @Parameter(description = "Filter by status")
            @RequestParam(required = false) VehicleStatus status,
            @Parameter(description = "Filter by inventory type")
            @RequestParam(required = false) InventoryType inventoryType,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default 20, max 100)")
            @RequestParam(defaultValue = "20") int size) {

        // Enforce max page size
        if (size > 100) {
            size = 100;
        }

        VehicleRepository.Page<VehicleResponse> result = vehicleService.listVehicles(
                tenantId, make, model, status, inventoryType, page, size);

        return ResponseEntity.ok(PagedResponse.of(result));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get vehicle by ID", description = "Retrieve a single vehicle by its ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved vehicle"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Vehicle not found")
    })
    public ResponseEntity<ApiResponse<VehicleResponse>> getVehicle(
            @Parameter(description = "Tenant ID", required = true)
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @Parameter(description = "Vehicle ID", required = true)
            @PathVariable UUID id) {

        VehicleResponse vehicle = vehicleService.getVehicle(id, tenantId);
        return ResponseEntity.ok(ApiResponse.of(vehicle));
    }

    @GetMapping("/search")
    @Operation(summary = "Search vehicle", description = "Search for a vehicle by VIN or license plate")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully found vehicle"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Must provide either vin or licensePlate"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Vehicle not found")
    })
    public ResponseEntity<ApiResponse<VehicleResponse>> searchVehicle(
            @Parameter(description = "Tenant ID", required = true)
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @Parameter(description = "Vehicle Identification Number")
            @RequestParam(required = false) String vin,
            @Parameter(description = "License plate number")
            @RequestParam(required = false) String licensePlate) {

        if (vin == null && licensePlate == null) {
            throw new IllegalArgumentException("Must provide either 'vin' or 'licensePlate' parameter");
        }

        VehicleResponse vehicle;
        if (vin != null) {
            vehicle = vehicleService.searchByVin(vin, tenantId);
        } else {
            vehicle = vehicleService.searchByLicensePlate(licensePlate, tenantId);
        }

        return ResponseEntity.ok(ApiResponse.of(vehicle));
    }

    @PostMapping
    @Operation(summary = "Create vehicle", description = "Create a new vehicle in the inventory")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Vehicle created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<ApiResponse<VehicleResponse>> createVehicle(
            @Parameter(description = "Tenant ID", required = true)
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @Valid @RequestBody CreateVehicleRequest request) {

        VehicleResponse vehicle = vehicleService.createVehicle(request, tenantId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(vehicle));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update vehicle", description = "Update an existing vehicle")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Vehicle updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Vehicle not found")
    })
    public ResponseEntity<ApiResponse<VehicleResponse>> updateVehicle(
            @Parameter(description = "Tenant ID", required = true)
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @Parameter(description = "Vehicle ID", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody UpdateVehicleRequest request) {

        VehicleResponse vehicle = vehicleService.updateVehicle(id, request, tenantId);
        return ResponseEntity.ok(ApiResponse.of(vehicle));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete vehicle", description = "Delete a vehicle from the inventory")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Vehicle deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Vehicle not found")
    })
    public ResponseEntity<Void> deleteVehicle(
            @Parameter(description = "Tenant ID", required = true)
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @Parameter(description = "Vehicle ID", required = true)
            @PathVariable UUID id) {

        vehicleService.deleteVehicle(id, tenantId);
        return ResponseEntity.noContent().build();
    }

    // ==================== Reservation Endpoints ====================

    @GetMapping("/{vehicleId}/reservations")
    @Operation(summary = "Get vehicle reservations", description = "Get all reservations for a specific vehicle")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved reservations"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Vehicle not found")
    })
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getReservationsForVehicle(
            @Parameter(description = "Tenant ID", required = true)
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @Parameter(description = "Vehicle ID", required = true)
            @PathVariable UUID vehicleId) {

        List<ReservationResponse> reservations = reservationService.getReservationsForVehicle(vehicleId, tenantId);
        return ResponseEntity.ok(ApiResponse.of(reservations));
    }

    @PostMapping("/{vehicleId}/reservations")
    @Operation(summary = "Create reservation", description = "Reserve a vehicle")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Reservation created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Vehicle not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Vehicle not available for reservation")
    })
    public ResponseEntity<ApiResponse<ReservationResponse>> createReservation(
            @Parameter(description = "Tenant ID", required = true)
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @Parameter(description = "Employee ID", required = true)
            @RequestHeader("X-Employee-Id") UUID employeeId,
            @Parameter(description = "Vehicle ID", required = true)
            @PathVariable UUID vehicleId,
            @Valid @RequestBody CreateReservationRequest request) {

        ReservationResponse reservation = reservationService.createReservation(vehicleId, request, tenantId, employeeId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(reservation));
    }

    @DeleteMapping("/reservations/{reservationId}")
    @Operation(summary = "Cancel reservation", description = "Cancel an existing reservation")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Reservation cancelled successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Reservation not found")
    })
    public ResponseEntity<Void> cancelReservation(
            @Parameter(description = "Tenant ID", required = true)
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @Parameter(description = "Reservation ID", required = true)
            @PathVariable UUID reservationId) {

        reservationService.cancelReservation(reservationId, tenantId);
        return ResponseEntity.noContent().build();
    }

    // ==================== Action Endpoints ====================

    @GetMapping("/{vehicleId}/actions")
    @Operation(summary = "Get vehicle actions", description = "Get action history for a specific vehicle (chronological order)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved actions"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Vehicle not found")
    })
    public ResponseEntity<ApiResponse<List<VehicleActionResponse>>> getActionsForVehicle(
            @Parameter(description = "Tenant ID", required = true)
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @Parameter(description = "Vehicle ID", required = true)
            @PathVariable UUID vehicleId) {

        List<VehicleActionResponse> actions = vehicleActionService.getActionsForVehicle(vehicleId, tenantId);
        return ResponseEntity.ok(ApiResponse.of(actions));
    }

    @PostMapping("/{vehicleId}/actions")
    @Operation(summary = "Log vehicle action", description = "Log an action/remark against a vehicle")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Action logged successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Vehicle not found")
    })
    public ResponseEntity<ApiResponse<VehicleActionResponse>> createAction(
            @Parameter(description = "Tenant ID", required = true)
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @Parameter(description = "Employee ID", required = true)
            @RequestHeader("X-Employee-Id") UUID employeeId,
            @Parameter(description = "Vehicle ID", required = true)
            @PathVariable UUID vehicleId,
            @Valid @RequestBody CreateVehicleActionRequest request) {

        VehicleActionResponse action = vehicleActionService.createAction(vehicleId, request, tenantId, employeeId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(action));
    }
}
