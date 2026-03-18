package com.keyloop.inventory.domain.service;

import com.keyloop.inventory.domain.model.Vehicle;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Domain service for calculating aging stock metrics.
 * Pure business logic - no framework dependencies.
 */
public class AgingStockCalculator {

    private static final long AGING_THRESHOLD_DAYS = 90;

    /**
     * Calculate the number of days a vehicle has been in inventory.
     * 
     * @param vehicle the vehicle to check
     * @return number of days since receivedDate, or 0 if receivedDate is null
     */
    public long calculateDaysInInventory(Vehicle vehicle) {
        return calculateDaysInInventory(vehicle, LocalDate.now());
    }

    /**
     * Calculate the number of days a vehicle has been in inventory as of a given date.
     * Useful for testing with specific dates.
     * 
     * @param vehicle the vehicle to check
     * @param asOfDate the date to calculate from
     * @return number of days since receivedDate, or 0 if receivedDate is null
     */
    public long calculateDaysInInventory(Vehicle vehicle, LocalDate asOfDate) {
        if (vehicle == null || vehicle.getReceivedDate() == null) {
            return 0;
        }
        
        LocalDate receivedDate = vehicle.getReceivedDate();
        if (receivedDate.isAfter(asOfDate)) {
            return 0;
        }
        
        return ChronoUnit.DAYS.between(receivedDate, asOfDate);
    }

    /**
     * Determine if a vehicle is considered aging stock (>90 days in inventory).
     * 
     * @param vehicle the vehicle to check
     * @return true if the vehicle has been in inventory for more than 90 days
     */
    public boolean isAgingStock(Vehicle vehicle) {
        return isAgingStock(vehicle, LocalDate.now());
    }

    /**
     * Determine if a vehicle is considered aging stock as of a given date.
     * Useful for testing with specific dates.
     * 
     * @param vehicle the vehicle to check
     * @param asOfDate the date to calculate from
     * @return true if the vehicle has been in inventory for more than 90 days
     */
    public boolean isAgingStock(Vehicle vehicle, LocalDate asOfDate) {
        long daysInInventory = calculateDaysInInventory(vehicle, asOfDate);
        return daysInInventory > AGING_THRESHOLD_DAYS;
    }

    /**
     * Get the aging threshold in days.
     * 
     * @return the number of days after which a vehicle is considered aging stock
     */
    public long getAgingThresholdDays() {
        return AGING_THRESHOLD_DAYS;
    }
}
