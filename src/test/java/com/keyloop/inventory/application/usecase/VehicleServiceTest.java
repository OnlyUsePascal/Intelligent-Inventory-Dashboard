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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private VehicleMapper vehicleMapper;

    @InjectMocks
    private VehicleService vehicleService;

    @Test
    void getVehicleReturnsMappedResponse() {
        UUID vehicleId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        Vehicle vehicle = Vehicle.builder().id(vehicleId).build();
        VehicleResponse response = new VehicleResponse();
        ReflectionTestUtils.setField(response, "id", vehicleId);

        when(vehicleRepository.findByIdAndTenantId(vehicleId, tenantId)).thenReturn(Optional.of(vehicle));
        when(vehicleMapper.toResponse(vehicle)).thenReturn(response);

        VehicleResponse result = vehicleService.getVehicle(vehicleId, tenantId);

        assertThat(result).isSameAs(response);
    }

    @Test
    void listVehiclesMapsPageContent() {
        UUID tenantId = UUID.randomUUID();
        Vehicle vehicle = Vehicle.builder().id(UUID.randomUUID()).build();
        VehicleResponse response = new VehicleResponse();
        ReflectionTestUtils.setField(response, "id", vehicle.getId());
        VehicleRepository.Page<Vehicle> page = VehicleRepository.Page.of(List.of(vehicle), 0, 10, 1);

        when(vehicleRepository.findByTenantIdWithFilters(tenantId, null, null, null, null, 0, 10))
                .thenReturn(page);
        when(vehicleMapper.toResponse(vehicle)).thenReturn(response);

        VehicleRepository.Page<VehicleResponse> result = vehicleService.listVehicles(
                tenantId, null, null, null, null, 0, 10);

        assertThat(result.content()).containsExactly(response);
        assertThat(result.totalElements()).isEqualTo(1);
    }

    @Test
    void searchByVinThrowsWhenMissing() {
        UUID tenantId = UUID.randomUUID();

        when(vehicleRepository.findByVinAndTenantId("VIN", tenantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.searchByVin("VIN", tenantId))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void createVehicleThrowsWhenVinExists() {
        UUID tenantId = UUID.randomUUID();
        CreateVehicleRequest request = new CreateVehicleRequest();
        ReflectionTestUtils.setField(request, "vin", "VIN12345678901234");
        ReflectionTestUtils.setField(request, "inventoryType", InventoryType.NEW);
        ReflectionTestUtils.setField(request, "make", "Honda");
        ReflectionTestUtils.setField(request, "model", "Civic");

        when(vehicleRepository.existsByVinAndTenantId("VIN12345678901234", tenantId)).thenReturn(true);

        assertThatThrownBy(() -> vehicleService.createVehicle(request, tenantId))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createVehicleSavesAndMapsResponse() {
        UUID tenantId = UUID.randomUUID();
        CreateVehicleRequest request = new CreateVehicleRequest();
        ReflectionTestUtils.setField(request, "vin", "VIN12345678901234");
        ReflectionTestUtils.setField(request, "licensePlate", "ABC123");
        ReflectionTestUtils.setField(request, "make", "Honda");
        ReflectionTestUtils.setField(request, "model", "Civic");
        ReflectionTestUtils.setField(request, "inventoryType", InventoryType.NEW);
        ReflectionTestUtils.setField(request, "receivedDate", LocalDate.now());

        Vehicle vehicle = Vehicle.builder().id(UUID.randomUUID()).build();
        Vehicle saved = Vehicle.builder().id(UUID.randomUUID()).build();
        VehicleResponse response = new VehicleResponse();
        ReflectionTestUtils.setField(response, "id", saved.getId());

        when(vehicleRepository.existsByVinAndTenantId("VIN12345678901234", tenantId)).thenReturn(false);
        when(vehicleMapper.toEntity(request, tenantId)).thenReturn(vehicle);
        when(vehicleRepository.save(vehicle)).thenReturn(saved);
        when(vehicleMapper.toResponse(saved)).thenReturn(response);

        VehicleResponse result = vehicleService.createVehicle(request, tenantId);

        assertThat(result).isSameAs(response);
    }

    @Test
    void updateVehiclePersistsChanges() {
        UUID vehicleId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UpdateVehicleRequest request = new UpdateVehicleRequest();
        ReflectionTestUtils.setField(request, "status", VehicleStatus.SOLD);
        Vehicle vehicle = Vehicle.builder().id(vehicleId).status(VehicleStatus.AVAILABLE).build();
        Vehicle saved = Vehicle.builder().id(vehicleId).status(VehicleStatus.SOLD).build();
        VehicleResponse response = new VehicleResponse();
        ReflectionTestUtils.setField(response, "id", vehicleId);

        when(vehicleRepository.findByIdAndTenantId(vehicleId, tenantId)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.save(vehicle)).thenReturn(saved);
        when(vehicleMapper.toResponse(saved)).thenReturn(response);

        VehicleResponse result = vehicleService.updateVehicle(vehicleId, request, tenantId);

        verify(vehicleMapper).updateEntity(vehicle, request);
        assertThat(result).isSameAs(response);
    }

    @Test
    void deleteVehicleThrowsWhenMissing() {
        UUID vehicleId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        when(vehicleRepository.existsByIdAndTenantId(vehicleId, tenantId)).thenReturn(false);

        assertThatThrownBy(() -> vehicleService.deleteVehicle(vehicleId, tenantId))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deleteVehicleDeletesWhenPresent() {
        UUID vehicleId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        when(vehicleRepository.existsByIdAndTenantId(vehicleId, tenantId)).thenReturn(true);

        vehicleService.deleteVehicle(vehicleId, tenantId);

        verify(vehicleRepository).deleteById(vehicleId);
    }
}
