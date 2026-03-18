package com.keyloop.inventory.infrastructure.persistence.repository;

import com.keyloop.inventory.infrastructure.persistence.entity.VehicleJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for VehicleJpaEntity.
 */
@Repository
public interface SpringDataVehicleRepository extends JpaRepository<VehicleJpaEntity, UUID>, 
        JpaSpecificationExecutor<VehicleJpaEntity> {

    Optional<VehicleJpaEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<VehicleJpaEntity> findByVinAndTenantId(String vin, UUID tenantId);

    @Query("SELECT v FROM VehicleJpaEntity v WHERE v.tenantId = :tenantId AND LOWER(v.licensePlate) = LOWER(:licensePlate)")
    Optional<VehicleJpaEntity> findByLicensePlateAndTenantId(@Param("licensePlate") String licensePlate, 
                                                              @Param("tenantId") UUID tenantId);

    List<VehicleJpaEntity> findByTenantId(UUID tenantId);

    Page<VehicleJpaEntity> findByTenantId(UUID tenantId, Pageable pageable);

    boolean existsByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsByVinAndTenantId(String vin, UUID tenantId);
}
