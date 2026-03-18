package com.keyloop.inventory.infrastructure.persistence.repository;

import com.keyloop.inventory.domain.model.InventoryType;
import com.keyloop.inventory.domain.model.Vehicle;
import com.keyloop.inventory.domain.model.VehicleStatus;
import com.keyloop.inventory.domain.repository.VehicleRepository;
import com.keyloop.inventory.infrastructure.persistence.entity.VehicleJpaEntity;
import com.keyloop.inventory.infrastructure.persistence.mapper.VehiclePersistenceMapper;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter implementation of VehicleRepository using Spring Data JPA.
 */
@Repository
@RequiredArgsConstructor
public class JpaVehicleRepository implements VehicleRepository {

    private final SpringDataVehicleRepository springDataRepository;
    private final VehiclePersistenceMapper mapper;

    @Override
    public Optional<Vehicle> findById(UUID id) {
        return springDataRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Vehicle> findByIdAndTenantId(UUID id, UUID tenantId) {
        return springDataRepository.findByIdAndTenantId(id, tenantId)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Vehicle> findByVinAndTenantId(String vin, UUID tenantId) {
        return springDataRepository.findByVinAndTenantId(vin, tenantId)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Vehicle> findByLicensePlateAndTenantId(String licensePlate, UUID tenantId) {
        return springDataRepository.findByLicensePlateAndTenantId(licensePlate, tenantId)
                .map(mapper::toDomain);
    }

    @Override
    public List<Vehicle> findByTenantId(UUID tenantId) {
        return springDataRepository.findByTenantId(tenantId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Page<Vehicle> findByTenantIdWithFilters(UUID tenantId, String make, String model,
                                                    VehicleStatus status, InventoryType inventoryType,
                                                    int page, int size) {
        Specification<VehicleJpaEntity> spec = buildSpecification(tenantId, make, model, status, inventoryType);
        var pageable = PageRequest.of(page, size);
        var springPage = springDataRepository.findAll(spec, pageable);

        List<Vehicle> content = springPage.getContent().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());

        return Page.of(content, page, size, springPage.getTotalElements());
    }

    @Override
    public long countByTenantIdWithFilters(UUID tenantId, String make, String model,
                                           VehicleStatus status, InventoryType inventoryType) {
        Specification<VehicleJpaEntity> spec = buildSpecification(tenantId, make, model, status, inventoryType);
        return springDataRepository.count(spec);
    }

    @Override
    public Vehicle save(Vehicle vehicle) {
        var entity = mapper.toEntity(vehicle);
        var saved = springDataRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public void deleteById(UUID id) {
        springDataRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return springDataRepository.existsById(id);
    }

    @Override
    public boolean existsByIdAndTenantId(UUID id, UUID tenantId) {
        return springDataRepository.existsByIdAndTenantId(id, tenantId);
    }

    @Override
    public boolean existsByVinAndTenantId(String vin, UUID tenantId) {
        return springDataRepository.existsByVinAndTenantId(vin, tenantId);
    }

    private Specification<VehicleJpaEntity> buildSpecification(UUID tenantId, String make, String model,
                                                                VehicleStatus status, InventoryType inventoryType) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Tenant ID is always required
            predicates.add(criteriaBuilder.equal(root.get("tenantId"), tenantId));

            if (make != null && !make.isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("make")),
                        "%" + make.toLowerCase() + "%"
                ));
            }

            if (model != null && !model.isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("model")),
                        "%" + model.toLowerCase() + "%"
                ));
            }

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status.name()));
            }

            if (inventoryType != null) {
                predicates.add(criteriaBuilder.equal(root.get("inventoryType"), inventoryType.name()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
