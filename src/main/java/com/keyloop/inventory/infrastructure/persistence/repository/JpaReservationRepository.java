package com.keyloop.inventory.infrastructure.persistence.repository;

import com.keyloop.inventory.domain.model.Reservation;
import com.keyloop.inventory.domain.model.ReservationStatus;
import com.keyloop.inventory.domain.repository.ReservationRepository;
import com.keyloop.inventory.infrastructure.persistence.mapper.ReservationPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter implementation of ReservationRepository using Spring Data JPA.
 */
@Repository
@RequiredArgsConstructor
public class JpaReservationRepository implements ReservationRepository {

    private final SpringDataReservationRepository springDataRepository;
    private final ReservationPersistenceMapper mapper;

    @Override
    public Optional<Reservation> findById(UUID id) {
        return springDataRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Reservation> findByVehicleId(UUID vehicleId) {
        return springDataRepository.findByVehicleIdOrderByReservationDateDesc(vehicleId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Reservation> findActiveByVehicleId(UUID vehicleId) {
        return springDataRepository.findActiveByVehicleId(vehicleId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Reservation> findActiveByVehicleIdAndEmployeeId(UUID vehicleId, UUID employeeId) {
        return springDataRepository.findActiveByVehicleIdAndEmployeeId(vehicleId, employeeId)
                .map(mapper::toDomain);
    }

    @Override
    public Reservation save(Reservation reservation) {
        var entity = mapper.toEntity(reservation);
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
    public boolean hasActiveReservation(UUID vehicleId) {
        return springDataRepository.hasActiveReservation(vehicleId);
    }

    @Override
    public List<Reservation> findExpiredReservations(Instant expiryTime) {
        return springDataRepository.findExpiredReservations(expiryTime).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public int expireReservations(Instant expiryTime) {
        return springDataRepository.expireReservations(expiryTime, ReservationStatus.EXPIRED);
    }
}
