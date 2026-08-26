package com.edmin.reservation_system.reservations;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateReservationRequest (
        @NotNull
        Long userId,
        @NotNull
        Long roomId,
        @NotNull
        @FutureOrPresent
        LocalDate startDate,
        @NotNull
        @FutureOrPresent
        LocalDate endDate
) {
}
