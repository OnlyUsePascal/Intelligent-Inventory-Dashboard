package com.keyloop.inventory.application.usecase;

import com.keyloop.inventory.domain.model.Reservation;
import com.keyloop.inventory.domain.repository.ReservationRepository;
import com.keyloop.inventory.domain.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Scheduled job service to expire reservations that have exceeded 5 days.
 * Runs daily at midnight.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationExpiryService {

    private static final int EXPIRY_DAYS = 5;
    
    private final ReservationRepository reservationRepository;
    private final VehicleRepository vehicleRepository;

    /**
     * Scheduled task that runs daily at midnight to expire old reservations.
     * Reservations are expired if they've exceeded their reservedUntilDate.
     */
    @Scheduled(cron = "0 0 0 * * *") // Every day at midnight
    @Transactional
    public void expireOldReservations() {
        log.info("Starting reservation expiry job");
        
        Instant expiryThreshold = Instant.now();
        
        // Find expired reservations to get the affected vehicle IDs
        List<Reservation> expiredReservations = reservationRepository.findExpiredReservations(expiryThreshold);
        
        if (expiredReservations.isEmpty()) {
            log.info("No reservations to expire");
            return;
        }
        
        // Get unique vehicle IDs
        Set<UUID> affectedVehicleIds = expiredReservations.stream()
                .map(Reservation::getVehicleId)
                .collect(Collectors.toSet());
        
        // Bulk update reservations to EXPIRED status
        int expiredCount = reservationRepository.expireReservations(expiryThreshold);
        log.info("Expired {} reservations", expiredCount);
        
        // Update vehicle statuses for vehicles with no remaining active reservations
        int vehiclesUpdated = 0;
        for (UUID vehicleId : affectedVehicleIds) {
            if (!reservationRepository.hasActiveReservation(vehicleId)) {
                vehicleRepository.findById(vehicleId).ifPresent(vehicle -> {
                    vehicle.makeAvailable();
                    vehicleRepository.save(vehicle);
                });
                vehiclesUpdated++;
            }
        }
        
        log.info("Reservation expiry job completed. Expired {} reservations, updated {} vehicles to AVAILABLE", 
                expiredCount, vehiclesUpdated);
    }

    /**
     * Manual method to run expiry (useful for testing or admin triggers).
     */
    @Transactional
    public int runExpiryManually() {
        log.info("Manual reservation expiry triggered");
        expireOldReservations();
        return reservationRepository.expireReservations(Instant.now());
    }
}
