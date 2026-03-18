package com.keyloop.inventory.infrastructure.persistence.repository;

import com.keyloop.inventory.infrastructure.persistence.entity.VehicleActionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for VehicleActionJpaEntity.
 */
@Repository
public interface SpringDataVehicleActionRepository extends JpaRepository<VehicleActionJpaEntity, UUID> {

    List<VehicleActionJpaEntity> findByVehicleIdOrderByTimestampDesc(UUID vehicleId);

    List<VehicleActionJpaEntity> findByVehicleIdOrderByTimestampAsc(UUID vehicleId);
}
