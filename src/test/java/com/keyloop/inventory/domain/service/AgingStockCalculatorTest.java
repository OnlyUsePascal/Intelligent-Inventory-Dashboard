package com.keyloop.inventory.domain.service;

import com.keyloop.inventory.domain.model.Vehicle;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AgingStockCalculatorTest {

    private final AgingStockCalculator calculator = new AgingStockCalculator();

    @Test
    void calculateDaysInInventoryReturnsZeroWhenVehicleNull() {
        long days = calculator.calculateDaysInInventory(null, LocalDate.now());

        assertThat(days).isZero();
    }

    @Test
    void calculateDaysInInventoryReturnsZeroWhenReceivedDateNull() {
        Vehicle vehicle = Vehicle.builder()
                .id(UUID.randomUUID())
                .build();

        long days = calculator.calculateDaysInInventory(vehicle, LocalDate.now());

        assertThat(days).isZero();
    }

    @Test
    void calculateDaysInInventoryReturnsZeroWhenReceivedDateAfterAsOfDate() {
        LocalDate asOfDate = LocalDate.of(2024, 1, 10);
        Vehicle vehicle = Vehicle.builder()
                .receivedDate(LocalDate.of(2024, 1, 11))
                .build();

        long days = calculator.calculateDaysInInventory(vehicle, asOfDate);

        assertThat(days).isZero();
    }

    @Test
    void isAgingStockReturnsTrueWhenMoreThanThreshold() {
        LocalDate asOfDate = LocalDate.of(2024, 6, 30);
        Vehicle vehicle = Vehicle.builder()
                .receivedDate(LocalDate.of(2024, 3, 31))
                .build();

        boolean agingStock = calculator.isAgingStock(vehicle, asOfDate);

        assertThat(agingStock).isTrue();
    }

    @Test
    void isAgingStockReturnsFalseWhenAtOrBelowThreshold() {
        LocalDate asOfDate = LocalDate.of(2024, 6, 29);
        Vehicle vehicle = Vehicle.builder()
                .receivedDate(LocalDate.of(2024, 3, 31))
                .build();

        boolean agingStock = calculator.isAgingStock(vehicle, asOfDate);

        assertThat(agingStock).isFalse();
    }
}
