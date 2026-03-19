package com.keyloop.inventory.application.mapper;

import com.keyloop.inventory.application.dto.request.CreateReservationRequest;
import com.keyloop.inventory.application.dto.response.ReservationResponse;
import com.keyloop.inventory.domain.model.Reservation;
import com.keyloop.inventory.domain.model.ReservationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationMapperTest {

    private final ReservationMapper mapper = new ReservationMapper();

    @Test
    void toResponseMapsStatusAndActiveFlag() {
        Instant future = Instant.now().plusSeconds(3600);
        Reservation reservation = Reservation.builder()
                .id(UUID.randomUUID())
                .vehicleId(UUID.randomUUID())
                .employeeId(UUID.randomUUID())
                .reservationDate(Instant.now())
                .reservedUntilDate(future)
                .status(ReservationStatus.ACTIVE)
                .build();

        ReservationResponse response = mapper.toResponse(reservation);

        assertThat(response)
                .hasFieldOrPropertyWithValue("status", ReservationStatus.ACTIVE);
        assertThat(response)
                .hasFieldOrPropertyWithValue("active", true);
    }

    @Test
    void toEntityDefaultsStatusToActive() {
        CreateReservationRequest request = new CreateReservationRequest();
        Instant reservedUntil = Instant.now().plusSeconds(7200);
        ReflectionTestUtils.setField(request, "reservedUntilDate", reservedUntil);

        Reservation reservation = mapper.toEntity(request, UUID.randomUUID(), UUID.randomUUID());

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.ACTIVE);
        assertThat(reservation.getReservedUntilDate()).isEqualTo(reservedUntil);
    }
}
