package com.edmin.reservation_system.rooms;

public record RoomSearchFilter(
        String name,
        RoomType type,
        Long capacity,
        Integer pageSize,
        Integer pageNumber
) {
}
