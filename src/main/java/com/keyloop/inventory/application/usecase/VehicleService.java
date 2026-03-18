package com.keyloop.inventory.application.usecase;

import com.keyloop.inventory.application.dto.request.CreateVehicleRequest;
import com.keyloop.inventory.application.dto.request.UpdateVehicleRequest;
import com.keyloop.inventory.application.dto.response.VehicleResponse;
import com.keyloop.inventory.application.mapper.VehicleMapper;
import com.keyloop.inventory.domain.exception.EntityNotFoundException;
import com.keyloop.inventory.domain.model.InventoryType;
import com.keyloop.inventory.domain.model.Vehicle;
import com.keyloop.inventory.domain.model.VehicleStatus;
import com.keyloop.inventory.domain.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Application service for vehicle use cases.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleMapper vehicleMapper;

    @Transactional(readOnly = true)
    public VehicleResponse getVehicle(UUID vehicleId, UUID tenantId) {
        Vehicle vehicle = vehicleRepository.findByIdAndTenantId(vehicleId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle", vehicleId));
        return vehicleMapper.toResponse(vehicle);
    }

    @Transactional(readOnly = true)
    public VehicleRepository.Page<VehicleResponse> listVehicles(UUID tenantId, String make, String model,
                                                                 VehicleStatus status, InventoryType inventoryType,
                                                                 int page, int size) {
        var domainPage = vehicleRepository.findByTenantIdWithFilters(
                tenantId, make, model, status, inventoryType, page, size);
        
        List<VehicleResponse> content = domainPage.content().stream()
                .map(vehicleMapper::toResponse)
                .collect(Collectors.toList());
        
        return VehicleRepository.Page.of(content, page, size, domainPage.totalElements());
    }

    @Transactional(readOnly = true)
    public VehicleResponse searchByVin(String vin, UUID tenantId) {
        Vehicle vehicle = vehicleRepository.findByVinAndTenantId(vin, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle", "vin", vin));
        return vehicleMapper.toResponse(vehicle);
    }

    @Transactional(readOnly = true)
    public VehicleResponse searchByLicensePlate(String licensePlate, UUID tenantId) {
        Vehicle vehicle = vehicleRepository.findByLicensePlateAndTenantId(licensePlate, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle", "licensePlate", licensePlate));
        return vehicleMapper.toResponse(vehicle);
    }

    public VehicleResponse createVehicle(CreateVehicleRequest request, UUID tenantId) {
        // Check if VIN already exists for this tenant
        if (vehicleRepository.existsByVinAndTenantId(request.getVin(), tenantId)) {
            throw new IllegalArgumentException("Vehicle with VIN " + request.getVin() + " already exists");
        }

        Vehicle vehicle = vehicleMapper.toEntity(request, tenantId);
        Vehicle saved = vehicleRepository.save(vehicle);
        return vehicleMapper.toResponse(saved);
    }

    public VehicleResponse updateVehicle(UUID vehicleId, UpdateVehicleRequest request, UUID tenantId) {
        Vehicle vehicle = vehicleRepository.findByIdAndTenantId(vehicleId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle", vehicleId));
        
        vehicleMapper.updateEntity(vehicle, request);
        Vehicle saved = vehicleRepository.save(vehicle);
        return vehicleMapper.toResponse(saved);
    }

    public void deleteVehicle(UUID vehicleId, UUID tenantId) {
        if (!vehicleRepository.existsByIdAndTenantId(vehicleId, tenantId)) {
            throw new EntityNotFoundException("Vehicle", vehicleId);
        }
        vehicleRepository.deleteById(vehicleId);
    }
}
