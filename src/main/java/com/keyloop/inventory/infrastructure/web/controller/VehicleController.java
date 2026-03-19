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
import com.keyloop.inventory.infrastructure.security.RoleGuard;
import com.keyloop.inventory.infrastructure.security.TenantContext;
import com.keyloop.inventory.infrastructure.web.dto.ApiResponse;
import com.keyloop.inventory.infrastructure.web.dto.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
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
public class VehicleController {

    private final VehicleService vehicleService;
    private final ReservationService reservationService;
    private final VehicleActionService vehicleActionService;

    public VehicleController(VehicleService vehicleService,
                             ReservationService reservationService,
                             VehicleActionService vehicleActionService) {
        this.vehicleService = vehicleService;
        this.reservationService = reservationService;
        this.vehicleActionService = vehicleActionService;
    }

    // ==================== Vehicle Endpoints ====================

    @GetMapping
    @Operation(summary = "List vehicles", description = "Get paginated list of vehicles with optional filters", tags = {"Vehicles"})
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved vehicles"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request parameters",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<PagedResponse<VehicleResponse>> listVehicles(
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

        // All authenticated roles can list vehicles
        UUID tenantId = TenantContext.getTenantId();

        // Enforce max page size
        if (size > 100) {
            size = 100;
        }

        VehicleRepository.Page<VehicleResponse> result = vehicleService.listVehicles(
                tenantId, make, model, status, inventoryType, page, size);

        return ResponseEntity.ok(PagedResponse.of(result));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get vehicle by ID", description = "Retrieve a single vehicle by its ID", tags = {"Vehicles"})
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved vehicle"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid vehicle ID",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Vehicle not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<ApiResponse<VehicleResponse>> getVehicle(
            @Parameter(description = "Vehicle ID", required = true)
            @PathVariable UUID id) {

        // All authenticated roles can view a vehicle
        UUID tenantId = TenantContext.getTenantId();

        VehicleResponse vehicle = vehicleService.getVehicle(id, tenantId);
        return ResponseEntity.ok(ApiResponse.of(vehicle));
    }

    @GetMapping("/search")
    @Operation(summary = "Search vehicle", description = "Search for a vehicle by VIN or license plate", tags = {"Vehicles"})
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully found vehicle"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Must provide either vin or licensePlate",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Vehicle not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<ApiResponse<VehicleResponse>> searchVehicle(
            @Parameter(description = "Vehicle Identification Number")
            @RequestParam(required = false) String vin,
            @Parameter(description = "License plate number")
            @RequestParam(required = false) String licensePlate) {

        // All authenticated roles can search vehicles
        UUID tenantId = TenantContext.getTenantId();

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
    @Operation(summary = "Create vehicle", description = "Create a new vehicle in the inventory", tags = {"Vehicles"})
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Vehicle payload",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CreateVehicleRequest.class),
                    examples = @ExampleObject(
                            name = "CreateVehicleRequest",
                            value = "{\"vin\":\"1HGCM82633A000001\",\"licensePlate\":\"KLP-104\",\"make\":\"Toyota\",\"model\":\"Corolla\",\"year\":2022,\"mileage\":12000,\"inventoryType\":\"USED\",\"status\":\"AVAILABLE\",\"receivedDate\":\"2025-12-02\",\"availableForSaleDate\":\"2025-12-10\"}"
                    )
            )
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Vehicle created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "CreateVehicleResponse",
                                    value = "{\"data\":{\"id\":\"d1f1c7b7-5f0b-4f5b-9d1a-1b7f2a233c61\",\"vin\":\"1HGCM82633A000001\",\"licensePlate\":\"KLP-104\",\"make\":\"Toyota\",\"model\":\"Corolla\",\"year\":2022,\"mileage\":12000,\"status\":\"AVAILABLE\",\"inventoryType\":\"USED\",\"receivedDate\":\"2025-12-02\",\"availableForSaleDate\":\"2025-12-10\",\"isAgingStock\":false,\"daysInInventory\":28,\"createdAt\":\"2025-12-30T09:12:45Z\",\"updatedAt\":\"2025-12-30T09:12:45Z\"},\"meta\":{\"timestamp\":\"2025-12-30T09:12:45Z\"}}"
                            )
                    )),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient permissions",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<ApiResponse<VehicleResponse>> createVehicle(
            @Valid @RequestBody CreateVehicleRequest request) {

        // Only ADMIN and INVENTORY can create vehicles
        RoleGuard.requireAdminOrInventory();
        UUID tenantId = TenantContext.getTenantId();

        VehicleResponse vehicle = vehicleService.createVehicle(request, tenantId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(vehicle));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update vehicle", description = "Update an existing vehicle", tags = {"Vehicles"})
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Vehicle updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient permissions",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Vehicle not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<ApiResponse<VehicleResponse>> updateVehicle(
            @Parameter(description = "Vehicle ID", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody UpdateVehicleRequest request) {

        // Only ADMIN and INVENTORY can update vehicles
        RoleGuard.requireAdminOrInventory();
        UUID tenantId = TenantContext.getTenantId();

        VehicleResponse vehicle = vehicleService.updateVehicle(id, request, tenantId);
        return ResponseEntity.ok(ApiResponse.of(vehicle));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete vehicle", description = "Delete a vehicle from the inventory", tags = {"Vehicles"})
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Vehicle deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient permissions",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Vehicle not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<Void> deleteVehicle(
            @Parameter(description = "Vehicle ID", required = true)
            @PathVariable UUID id) {

        // Only ADMIN can delete vehicles
        RoleGuard.requireAdmin();
        UUID tenantId = TenantContext.getTenantId();

        vehicleService.deleteVehicle(id, tenantId);
        return ResponseEntity.noContent().build();
    }

    // ==================== Reservation Endpoints ====================

    @GetMapping("/{vehicleId}/reservations")
    @Operation(summary = "Get vehicle reservations", description = "Get all reservations for a specific vehicle", tags = {"Reservations"})
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved reservations"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Vehicle not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getReservationsForVehicle(
            @Parameter(description = "Vehicle ID", required = true)
            @PathVariable UUID vehicleId) {

        // All authenticated roles can view reservations
        UUID tenantId = TenantContext.getTenantId();

        List<ReservationResponse> reservations = reservationService.getReservationsForVehicle(vehicleId, tenantId);
        return ResponseEntity.ok(ApiResponse.of(reservations));
    }

    @PostMapping("/{vehicleId}/reservations")
    @Operation(summary = "Create reservation", description = "Reserve a vehicle", tags = {"Reservations"})
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Reservation payload",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CreateReservationRequest.class),
                    examples = @ExampleObject(
                            name = "CreateReservationRequest",
                            value = "{\"reservedUntilDate\":\"2026-01-12T17:00:00Z\"}"
                    )
            )
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Reservation created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "CreateReservationResponse",
                                    value = "{\"data\":{\"id\":\"8bce2ed0-2bfa-4fb2-8f88-2b136a4f93fd\",\"vehicleId\":\"9f71b46c-6bd0-4b0e-a1db-591f705893c2\",\"employeeId\":\"550e8400-e29b-41d4-a716-446655440003\",\"reservationDate\":\"2025-12-30T10:10:00Z\",\"reservedUntilDate\":\"2026-01-12T17:00:00Z\",\"status\":\"ACTIVE\",\"active\":true,\"createdAt\":\"2025-12-30T10:10:00Z\",\"updatedAt\":\"2025-12-30T10:10:00Z\"},\"meta\":{\"timestamp\":\"2025-12-30T10:10:00Z\"}}"
                            )
                    )),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient permissions",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Vehicle not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Vehicle not available for reservation",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<ApiResponse<ReservationResponse>> createReservation(
            @Parameter(description = "Vehicle ID", required = true)
            @PathVariable UUID vehicleId,
            @Valid @RequestBody CreateReservationRequest request) {

        // Only ADMIN and SALE can create reservations
        RoleGuard.requireAdminOrSale();
        UUID tenantId = TenantContext.getTenantId();
        UUID employeeId = TenantContext.getEmployeeId();

        ReservationResponse reservation = reservationService.createReservation(vehicleId, request, tenantId, employeeId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(reservation));
    }

    @DeleteMapping("/reservations/{reservationId}")
    @Operation(summary = "Cancel reservation", description = "Cancel an existing reservation", tags = {"Reservations"})
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Reservation cancelled successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient permissions",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Reservation not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<Void> cancelReservation(
            @Parameter(description = "Reservation ID", required = true)
            @PathVariable UUID reservationId) {

        // Only ADMIN and SALE can cancel reservations
        RoleGuard.requireAdminOrSale();
        UUID tenantId = TenantContext.getTenantId();
        UUID employeeId = TenantContext.getEmployeeId();

        reservationService.cancelReservation(reservationId, tenantId, employeeId);
        return ResponseEntity.noContent().build();
    }

    // ==================== Action Endpoints ====================

    @GetMapping("/{vehicleId}/actions")
    @Operation(summary = "Get vehicle actions", description = "Get action history for a specific vehicle (chronological order)", tags = {"Actions"})
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved actions"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Vehicle not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<ApiResponse<List<VehicleActionResponse>>> getActionsForVehicle(
            @Parameter(description = "Vehicle ID", required = true)
            @PathVariable UUID vehicleId) {

        // All authenticated roles can view actions
        UUID tenantId = TenantContext.getTenantId();

        List<VehicleActionResponse> actions = vehicleActionService.getActionsForVehicle(vehicleId, tenantId);
        return ResponseEntity.ok(ApiResponse.of(actions));
    }

    @PostMapping("/{vehicleId}/actions")
    @Operation(summary = "Log vehicle action", description = "Log an action/remark against a vehicle", tags = {"Actions"})
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Action payload",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CreateVehicleActionRequest.class),
                    examples = @ExampleObject(
                            name = "CreateVehicleActionRequest",
                            value = "{\"actionText\":\"Customer requested minor paint touch-up before delivery.\"}"
                    )
            )
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Action logged successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "CreateVehicleActionResponse",
                                    value = "{\"data\":{\"id\":\"7e1a468b-05f2-4d24-9cd5-4ed1b6a1b2a5\",\"vehicleId\":\"9f71b46c-6bd0-4b0e-a1db-591f705893c2\",\"employeeId\":\"550e8400-e29b-41d4-a716-446655440002\",\"actionText\":\"Customer requested minor paint touch-up before delivery.\",\"timestamp\":\"2025-12-30T12:30:00Z\",\"createdAt\":\"2025-12-30T12:30:00Z\",\"updatedAt\":\"2025-12-30T12:30:00Z\"},\"meta\":{\"timestamp\":\"2025-12-30T12:30:00Z\"}}"
                            )
                    )),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Vehicle not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<ApiResponse<VehicleActionResponse>> createAction(
            @Parameter(description = "Vehicle ID", required = true)
            @PathVariable UUID vehicleId,
            @Valid @RequestBody CreateVehicleActionRequest request) {

        // All authenticated roles can log actions
        UUID tenantId = TenantContext.getTenantId();
        UUID employeeId = TenantContext.getEmployeeId();

        VehicleActionResponse action = vehicleActionService.createAction(vehicleId, request, tenantId, employeeId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(action));
    }
}
