package com.edmin.reservation_system.rooms;

public record RoomResponse (
        Long id,
        String name,
        RoomType type,
        Long capacity,
        RoomStatus status
        )
{}
