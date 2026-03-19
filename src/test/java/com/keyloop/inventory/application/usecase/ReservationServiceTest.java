package com.keyloop.inventory.application.usecase;

import com.keyloop.inventory.application.dto.request.CreateReservationRequest;
import com.keyloop.inventory.application.dto.response.ReservationResponse;
import com.keyloop.inventory.application.mapper.ReservationMapper;
import com.keyloop.inventory.domain.exception.EntityNotFoundException;
import com.keyloop.inventory.domain.exception.VehicleNotAvailableException;
import com.keyloop.inventory.domain.model.EmployeeRole;
import com.keyloop.inventory.domain.model.Reservation;
import com.keyloop.inventory.domain.model.ReservationStatus;
import com.keyloop.inventory.domain.model.Vehicle;
import com.keyloop.inventory.domain.model.VehicleStatus;
import com.keyloop.inventory.domain.repository.ReservationRepository;
import com.keyloop.inventory.domain.repository.VehicleRepository;
import com.keyloop.inventory.infrastructure.security.TenantContext;
import com.keyloop.inventory.infrastructure.security.exception.ForbiddenException;
import org.junit.jupiter.api.AfterEach;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private ReservationMapper reservationMapper;

    @InjectMocks
    private ReservationService reservationService;

    @AfterEach
    void clearTenantContext() {
        TenantContext.clear();
    }

    @Test
    void getReservationsForVehicleThrowsWhenVehicleMissing() {
        UUID vehicleId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        when(vehicleRepository.existsByIdAndTenantId(vehicleId, tenantId)).thenReturn(false);

        assertThatThrownBy(() -> reservationService.getReservationsForVehicle(vehicleId, tenantId))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getReservationsForVehicleMapsResponses() {
        UUID vehicleId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        Reservation reservation = Reservation.builder().id(UUID.randomUUID()).build();
        ReservationResponse response = new ReservationResponse();
        ReflectionTestUtils.setField(response, "id", reservation.getId());

        when(vehicleRepository.existsByIdAndTenantId(vehicleId, tenantId)).thenReturn(true);
        when(reservationRepository.findByVehicleId(vehicleId)).thenReturn(List.of(reservation));
        when(reservationMapper.toResponse(reservation)).thenReturn(response);

        List<ReservationResponse> result = reservationService.getReservationsForVehicle(vehicleId, tenantId);

        assertThat(result).containsExactly(response);
    }

    @Test
    void createReservationThrowsWhenVehicleUnavailable() {
        UUID vehicleId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        Vehicle vehicle = Vehicle.builder().id(vehicleId).status(VehicleStatus.SOLD).build();

        when(vehicleRepository.findByIdAndTenantId(vehicleId, tenantId)).thenReturn(Optional.of(vehicle));

        assertThatThrownBy(() -> reservationService.createReservation(vehicleId, new CreateReservationRequest(), tenantId, UUID.randomUUID()))
                .isInstanceOf(VehicleNotAvailableException.class);
    }

    @Test
    void createReservationSavesReservationAndReservesVehicle() {
        UUID vehicleId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID employeeId = UUID.randomUUID();
        CreateReservationRequest request = new CreateReservationRequest();
        ReflectionTestUtils.setField(request, "reservedUntilDate", Instant.now().plusSeconds(3600));
        Vehicle vehicle = Vehicle.builder().id(vehicleId).status(VehicleStatus.AVAILABLE).build();
        Reservation reservation = Reservation.builder().id(UUID.randomUUID()).build();
        Reservation saved = Reservation.builder().id(UUID.randomUUID()).build();
        ReservationResponse response = new ReservationResponse();
        ReflectionTestUtils.setField(response, "id", saved.getId());

        when(vehicleRepository.findByIdAndTenantId(vehicleId, tenantId)).thenReturn(Optional.of(vehicle));
        when(reservationMapper.toEntity(request, vehicleId, employeeId)).thenReturn(reservation);
        when(reservationRepository.save(reservation)).thenReturn(saved);
        when(reservationMapper.toResponse(saved)).thenReturn(response);

        ReservationResponse result = reservationService.createReservation(vehicleId, request, tenantId, employeeId);

        assertThat(result).isSameAs(response);
        verify(vehicleRepository).save(vehicle);
        assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.RESERVED);
    }

    @Test
    void cancelReservationThrowsWhenAlreadyCancelled() {
        UUID reservationId = UUID.randomUUID();
        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .vehicleId(UUID.randomUUID())
                .employeeId(UUID.randomUUID())
                .status(ReservationStatus.CANCELLED)
                .build();

        UUID tenantId = UUID.randomUUID();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(vehicleRepository.existsByIdAndTenantId(reservation.getVehicleId(), tenantId))
                .thenReturn(true);

        assertThatThrownBy(() -> reservationService.cancelReservation(reservationId, tenantId, reservation.getEmployeeId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cancelReservationThrowsWhenNotCreatorOrAdmin() {
        UUID reservationId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();
        UUID otherEmployeeId = UUID.randomUUID();
        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .vehicleId(UUID.randomUUID())
                .employeeId(creatorId)
                .status(ReservationStatus.ACTIVE)
                .build();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(vehicleRepository.existsByIdAndTenantId(reservation.getVehicleId(), tenantId)).thenReturn(true);

        assertThatThrownBy(() -> reservationService.cancelReservation(reservationId, tenantId, otherEmployeeId))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void cancelReservationAllowsAdmin() {
        UUID reservationId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID employeeId = UUID.randomUUID();
        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .vehicleId(UUID.randomUUID())
                .employeeId(UUID.randomUUID())
                .status(ReservationStatus.ACTIVE)
                .build();

        TenantContext.set(tenantId, employeeId, EmployeeRole.ADMIN);
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(vehicleRepository.existsByIdAndTenantId(reservation.getVehicleId(), tenantId)).thenReturn(true);
        when(reservationRepository.hasActiveReservation(reservation.getVehicleId())).thenReturn(true);

        reservationService.cancelReservation(reservationId, tenantId, employeeId);

        verify(reservationRepository).save(reservation);
        verify(vehicleRepository, never()).save(any());
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
    }

    @Test
    void cancelReservationMakesVehicleAvailableWhenNoOtherActiveReservations() {
        UUID reservationId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID employeeId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .vehicleId(vehicleId)
                .employeeId(employeeId)
                .status(ReservationStatus.ACTIVE)
                .build();
        Vehicle vehicle = Vehicle.builder().id(vehicleId).status(VehicleStatus.RESERVED).build();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(vehicleRepository.existsByIdAndTenantId(vehicleId, tenantId)).thenReturn(true);
        when(reservationRepository.hasActiveReservation(vehicleId)).thenReturn(false);
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicle));

        reservationService.cancelReservation(reservationId, tenantId, employeeId);

        assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
        verify(vehicleRepository).save(vehicle);
    }
}
