package com.keyloop.inventory.infrastructure.persistence.repository;

import com.keyloop.inventory.domain.model.VehicleAction;
import com.keyloop.inventory.domain.repository.VehicleActionRepository;
import com.keyloop.inventory.infrastructure.persistence.mapper.VehicleActionPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter implementation of VehicleActionRepository using Spring Data JPA.
 */
@Repository
@RequiredArgsConstructor
public class JpaVehicleActionRepository implements VehicleActionRepository {

    private final SpringDataVehicleActionRepository springDataRepository;
    private final VehicleActionPersistenceMapper mapper;

    @Override
    public Optional<VehicleAction> findById(UUID id) {
        return springDataRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<VehicleAction> findByVehicleIdOrderByTimestampDesc(UUID vehicleId) {
        return springDataRepository.findByVehicleIdOrderByTimestampDesc(vehicleId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<VehicleAction> findByVehicleIdOrderByTimestampAsc(UUID vehicleId) {
        return springDataRepository.findByVehicleIdOrderByTimestampAsc(vehicleId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public VehicleAction save(VehicleAction vehicleAction) {
        var entity = mapper.toEntity(vehicleAction);
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
}
