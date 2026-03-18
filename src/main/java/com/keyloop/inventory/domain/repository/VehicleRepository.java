package com.keyloop.inventory.domain.repository;

import com.keyloop.inventory.domain.model.InventoryType;
import com.keyloop.inventory.domain.model.Vehicle;
import com.keyloop.inventory.domain.model.VehicleStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface (port) for Vehicle entity.
 * Implementations are provided in the infrastructure layer.
 */
public interface VehicleRepository {

    Optional<Vehicle> findById(UUID id);

    Optional<Vehicle> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<Vehicle> findByVinAndTenantId(String vin, UUID tenantId);

    Optional<Vehicle> findByLicensePlateAndTenantId(String licensePlate, UUID tenantId);

    List<Vehicle> findByTenantId(UUID tenantId);

    /**
     * Find vehicles with filtering and pagination.
     * 
     * @param tenantId required tenant ID for isolation
     * @param make optional filter by make
     * @param model optional filter by model
     * @param status optional filter by status
     * @param inventoryType optional filter by inventory type
     * @param page zero-based page number
     * @param size page size
     * @return page of vehicles matching the criteria
     */
    Page<Vehicle> findByTenantIdWithFilters(
            UUID tenantId,
            String make,
            String model,
            VehicleStatus status,
            InventoryType inventoryType,
            int page,
            int size
    );

    /**
     * Count vehicles with filtering.
     */
    long countByTenantIdWithFilters(
            UUID tenantId,
            String make,
            String model,
            VehicleStatus status,
            InventoryType inventoryType
    );

    Vehicle save(Vehicle vehicle);

    void deleteById(UUID id);

    boolean existsById(UUID id);

    boolean existsByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsByVinAndTenantId(String vin, UUID tenantId);

    /**
     * Simple page wrapper for domain layer.
     */
    record Page<T>(
            List<T> content,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {
        public static <T> Page<T> of(List<T> content, int page, int size, long totalElements) {
            int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
            return new Page<>(content, page, size, totalElements, totalPages);
        }
    }
}
