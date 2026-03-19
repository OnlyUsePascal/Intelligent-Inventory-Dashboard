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
import com.keyloop.inventory.domain.repository.ReservationRepository;
import com.keyloop.inventory.domain.repository.VehicleRepository;
import com.keyloop.inventory.infrastructure.security.TenantContext;
import com.keyloop.inventory.infrastructure.security.exception.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Application service for reservation use cases.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final VehicleRepository vehicleRepository;
    private final ReservationMapper reservationMapper;

    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservationsForVehicle(UUID vehicleId, UUID tenantId) {
        // Verify vehicle exists and belongs to tenant
        if (!vehicleRepository.existsByIdAndTenantId(vehicleId, tenantId)) {
            throw new EntityNotFoundException("Vehicle", vehicleId);
        }
        
        return reservationRepository.findByVehicleId(vehicleId).stream()
                .map(reservationMapper::toResponse)
                .collect(Collectors.toList());
    }

    public ReservationResponse createReservation(UUID vehicleId, CreateReservationRequest request, 
                                                  UUID tenantId, UUID employeeId) {
        // Get and validate vehicle
        Vehicle vehicle = vehicleRepository.findByIdAndTenantId(vehicleId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle", vehicleId));

        // Check if vehicle can be reserved
        if (!vehicle.canBeReserved()) {
            throw new VehicleNotAvailableException(vehicleId, vehicle.getStatus());
        }

        // Create reservation
        Reservation reservation = reservationMapper.toEntity(request, vehicleId, employeeId);
        Reservation saved = reservationRepository.save(reservation);

        // Update vehicle status
        vehicle.reserve();
        vehicleRepository.save(vehicle);

        return reservationMapper.toResponse(saved);
    }

    public void cancelReservation(UUID reservationId, UUID tenantId, UUID employeeId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("Reservation", reservationId));

        // Verify vehicle belongs to tenant
        if (!vehicleRepository.existsByIdAndTenantId(reservation.getVehicleId(), tenantId)) {
            throw new EntityNotFoundException("Reservation", reservationId);
        }

        // Check if reservation is already cancelled or expired
        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new IllegalStateException("Reservation is already " + reservation.getStatus().name().toLowerCase());
        }

        // Check permissions: only the creator or ADMIN can cancel
        boolean isCreator = reservation.getEmployeeId().equals(employeeId);
        boolean isAdmin = TenantContext.hasRole(EmployeeRole.ADMIN);
        
        if (!isCreator && !isAdmin) {
            throw new ForbiddenException("Only the reservation creator or an ADMIN can cancel this reservation");
        }

        // Cancel reservation (soft delete - keep history)
        reservation.cancel();
        reservationRepository.save(reservation);

        // Check if there are any other active reservations, if not, make vehicle available
        if (!reservationRepository.hasActiveReservation(reservation.getVehicleId())) {
            vehicleRepository.findById(reservation.getVehicleId())
                    .ifPresent(vehicle -> {
                        vehicle.makeAvailable();
                        vehicleRepository.save(vehicle);
                    });
        }
    }
}
