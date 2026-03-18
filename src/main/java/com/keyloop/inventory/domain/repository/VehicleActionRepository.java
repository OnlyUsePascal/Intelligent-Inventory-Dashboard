package com.keyloop.inventory.domain.repository;

import com.keyloop.inventory.domain.model.VehicleAction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface (port) for VehicleAction entity.
 * Implementations are provided in the infrastructure layer.
 */
public interface VehicleActionRepository {

    Optional<VehicleAction> findById(UUID id);

    /**
     * Find all actions for a vehicle, ordered by timestamp descending (most recent first).
     */
    List<VehicleAction> findByVehicleIdOrderByTimestampDesc(UUID vehicleId);

    /**
     * Find all actions for a vehicle, ordered by timestamp ascending (chronological).
     */
    List<VehicleAction> findByVehicleIdOrderByTimestampAsc(UUID vehicleId);

    VehicleAction save(VehicleAction vehicleAction);

    void deleteById(UUID id);

    boolean existsById(UUID id);
}
