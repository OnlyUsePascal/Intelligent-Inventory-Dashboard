package com.keyloop.inventory.application.usecase;

import com.keyloop.inventory.application.dto.request.CreateVehicleActionRequest;
import com.keyloop.inventory.application.dto.response.VehicleActionResponse;
import com.keyloop.inventory.application.mapper.VehicleActionMapper;
import com.keyloop.inventory.domain.exception.EntityNotFoundException;
import com.keyloop.inventory.domain.model.VehicleAction;
import com.keyloop.inventory.domain.repository.VehicleActionRepository;
import com.keyloop.inventory.domain.repository.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleActionServiceTest {

    @Mock
    private VehicleActionRepository vehicleActionRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private VehicleActionMapper vehicleActionMapper;

    @InjectMocks
    private VehicleActionService vehicleActionService;

    @Test
    void getActionsForVehicleThrowsWhenVehicleMissing() {
        UUID vehicleId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        when(vehicleRepository.existsByIdAndTenantId(vehicleId, tenantId)).thenReturn(false);

        assertThatThrownBy(() -> vehicleActionService.getActionsForVehicle(vehicleId, tenantId))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getActionsForVehicleReturnsChronologicalOrder() {
        UUID vehicleId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        VehicleAction first = VehicleAction.builder()
                .id(UUID.randomUUID())
                .timestamp(Instant.parse("2024-01-01T00:00:00Z"))
                .build();
        VehicleAction second = VehicleAction.builder()
                .id(UUID.randomUUID())
                .timestamp(Instant.parse("2024-01-02T00:00:00Z"))
                .build();
        VehicleActionResponse firstResponse = new VehicleActionResponse();
        ReflectionTestUtils.setField(firstResponse, "id", first.getId());
        VehicleActionResponse secondResponse = new VehicleActionResponse();
        ReflectionTestUtils.setField(secondResponse, "id", second.getId());

        when(vehicleRepository.existsByIdAndTenantId(vehicleId, tenantId)).thenReturn(true);
        when(vehicleActionRepository.findByVehicleIdOrderByTimestampAsc(vehicleId))
                .thenReturn(List.of(first, second));
        when(vehicleActionMapper.toResponse(first)).thenReturn(firstResponse);
        when(vehicleActionMapper.toResponse(second)).thenReturn(secondResponse);

        List<VehicleActionResponse> result = vehicleActionService.getActionsForVehicle(vehicleId, tenantId);

        assertThat(result).containsExactly(firstResponse, secondResponse);
    }

    @Test
    void createActionThrowsWhenVehicleMissing() {
        UUID vehicleId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        when(vehicleRepository.existsByIdAndTenantId(vehicleId, tenantId)).thenReturn(false);

        assertThatThrownBy(() -> vehicleActionService.createAction(vehicleId, new CreateVehicleActionRequest(), tenantId, UUID.randomUUID()))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void createActionSavesAndMapsResponse() {
        UUID vehicleId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID employeeId = UUID.randomUUID();
        CreateVehicleActionRequest request = new CreateVehicleActionRequest();
        ReflectionTestUtils.setField(request, "actionText", "checked in");
        VehicleAction action = VehicleAction.builder().id(UUID.randomUUID()).build();
        VehicleAction saved = VehicleAction.builder().id(UUID.randomUUID()).build();
        VehicleActionResponse response = new VehicleActionResponse();
        ReflectionTestUtils.setField(response, "id", saved.getId());

        when(vehicleRepository.existsByIdAndTenantId(vehicleId, tenantId)).thenReturn(true);
        when(vehicleActionMapper.toEntity(request, vehicleId, employeeId)).thenReturn(action);
        when(vehicleActionRepository.save(action)).thenReturn(saved);
        when(vehicleActionMapper.toResponse(saved)).thenReturn(response);

        VehicleActionResponse result = vehicleActionService.createAction(vehicleId, request, tenantId, employeeId);

        assertThat(result).isSameAs(response);
        verify(vehicleActionRepository).save(action);
    }
}
