package com.edmin.reservation_system.rooms;

import org.springframework.stereotype.Component;

@Component
public class RoomMapper {

    public RoomEntity toEntity(CreateRoomRequest request) {
        return new RoomEntity(
                null,
                request.name(),
                request.type(),
                request.capacity(),
                request.status()
        );
    }

    public RoomResponse toDomain(RoomEntity entity) {
        return new RoomResponse(
                entity.getId(),
                entity.getName(),
                entity.getType(),
                entity.getCapacity(),
                entity.getStatus()
        );
    }
}
