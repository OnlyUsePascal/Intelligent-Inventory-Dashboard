package com.keyloop.inventory.application.mapper;

import com.keyloop.inventory.application.dto.request.CreateVehicleRequest;
import com.keyloop.inventory.application.dto.request.UpdateVehicleRequest;
import com.keyloop.inventory.application.dto.response.VehicleResponse;
import com.keyloop.inventory.domain.model.InventoryType;
import com.keyloop.inventory.domain.model.Vehicle;
import com.keyloop.inventory.domain.model.VehicleStatus;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VehicleMapperTest {

    private final VehicleMapper mapper = new VehicleMapper();

    @Test
    void toResponseCalculatesAgingFlags() {
        LocalDate today = LocalDate.now();
        Vehicle vehicle = Vehicle.builder()
                .id(UUID.randomUUID())
                .vin("VIN12345678901234")
                .make("Ford")
                .model("Focus")
                .status(VehicleStatus.AVAILABLE)
                .inventoryType(InventoryType.USED)
                .receivedDate(today.minusDays(91))
                .build();

        VehicleResponse response = mapper.toResponse(vehicle);

        assertThat(response)
                .hasFieldOrPropertyWithValue("agingStock", true);
        long daysInInventory = (long) ReflectionTestUtils.getField(response, "daysInInventory");
        assertThat(daysInInventory).isGreaterThanOrEqualTo(91L);
    }

    @Test
    void toEntityDefaultsStatusWhenMissing() {
        CreateVehicleRequest request = new CreateVehicleRequest();
        ReflectionTestUtils.setField(request, "vin", "VIN12345678901234");
        ReflectionTestUtils.setField(request, "licensePlate", "ABC123");
        ReflectionTestUtils.setField(request, "make", "Toyota");
        ReflectionTestUtils.setField(request, "model", "Corolla");
        ReflectionTestUtils.setField(request, "inventoryType", InventoryType.NEW);

        Vehicle vehicle = mapper.toEntity(request, UUID.randomUUID());

        assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
    }

    @Test
    void updateEntityAppliesOnlyNonNullFields() {
        Vehicle vehicle = Vehicle.builder()
                .licensePlate("OLD")
                .make("Ford")
                .model("Focus")
                .year(2020)
                .mileage(10000)
                .status(VehicleStatus.AVAILABLE)
                .inventoryType(InventoryType.USED)
                .build();

        UpdateVehicleRequest request = new UpdateVehicleRequest();
        ReflectionTestUtils.setField(request, "licensePlate", "NEW");
        ReflectionTestUtils.setField(request, "mileage", 12000);

        mapper.updateEntity(vehicle, request);

        assertThat(vehicle.getLicensePlate()).isEqualTo("NEW");
        assertThat(vehicle.getMileage()).isEqualTo(12000);
        assertThat(vehicle.getMake()).isEqualTo("Ford");
        assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
    }
}
