package com.edmin.reservation_system.reservations;

import com.edmin.reservation_system.rooms.RoomEntity;
import com.edmin.reservation_system.users.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class ReservationMapper {
    public ReservationResponse toDomain(ReservationEntity reservationEntity) {
        return new ReservationResponse(
                reservationEntity.getId(),
                reservationEntity.getUser().getId(),
                reservationEntity.getRoom().getId(),
                reservationEntity.getStartDate(),
                reservationEntity.getEndDate(),
                reservationEntity.getStatus());
    }

    public ReservationEntity toEntity(UserEntity user, RoomEntity room, CreateReservationRequest reservationRequest) {
        return new ReservationEntity(
                null,
                user,
                room,
                reservationRequest.startDate(),
                reservationRequest.endDate(),
                ReservationStatus.PENDING);
    }
}
