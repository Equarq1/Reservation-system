package com.edmin.reservation_system.rooms;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

public record UpdateRoomRequest(

        @NotBlank
        String name,

        @NotNull
        RoomType type,

        @NotNull
        @Min(1)
        Long capacity

    )
{
}
