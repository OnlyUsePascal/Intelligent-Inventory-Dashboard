package com.keyloop.inventory.application.usecase;

import com.keyloop.inventory.application.dto.request.CreateVehicleActionRequest;
import com.keyloop.inventory.application.dto.response.VehicleActionResponse;
import com.keyloop.inventory.application.mapper.VehicleActionMapper;
import com.keyloop.inventory.domain.exception.EntityNotFoundException;
import com.keyloop.inventory.domain.model.VehicleAction;
import com.keyloop.inventory.domain.repository.VehicleActionRepository;
import com.keyloop.inventory.domain.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Application service for vehicle action (insight) use cases.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class VehicleActionService {

    private final VehicleActionRepository vehicleActionRepository;
    private final VehicleRepository vehicleRepository;
    private final VehicleActionMapper vehicleActionMapper;

    @Transactional(readOnly = true)
    public List<VehicleActionResponse> getActionsForVehicle(UUID vehicleId, UUID tenantId) {
        // Verify vehicle exists and belongs to tenant
        if (!vehicleRepository.existsByIdAndTenantId(vehicleId, tenantId)) {
            throw new EntityNotFoundException("Vehicle", vehicleId);
        }
        
        // Return actions in chronological order (oldest first)
        return vehicleActionRepository.findByVehicleIdOrderByTimestampAsc(vehicleId).stream()
                .map(vehicleActionMapper::toResponse)
                .collect(Collectors.toList());
    }

    public VehicleActionResponse createAction(UUID vehicleId, CreateVehicleActionRequest request,
                                               UUID tenantId, UUID employeeId) {
        // Verify vehicle exists and belongs to tenant
        if (!vehicleRepository.existsByIdAndTenantId(vehicleId, tenantId)) {
            throw new EntityNotFoundException("Vehicle", vehicleId);
        }

        VehicleAction action = vehicleActionMapper.toEntity(request, vehicleId, employeeId);
        VehicleAction saved = vehicleActionRepository.save(action);
        return vehicleActionMapper.toResponse(saved);
    }
}
