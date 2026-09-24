package com.edmin.reservation_system.rooms;

import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest (

        @NotNull
        RoomStatus status
)
{

}
