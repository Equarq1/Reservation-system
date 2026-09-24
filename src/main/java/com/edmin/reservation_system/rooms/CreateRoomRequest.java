package com.edmin.reservation_system.rooms;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateRoomRequest(
        @NotNull
        String name,

        @NotNull
        RoomType type,

        @NotNull
        @Min(1)
        Long capacity,

        @NotNull
        RoomStatus status
) {
}
