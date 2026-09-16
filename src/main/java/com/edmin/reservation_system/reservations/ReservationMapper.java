package com.edmin.reservation_system.reservations;

import org.springframework.stereotype.Component;

@Component
public class ReservationMapper {
    public ReservationResponse toDomain(ReservationEntity reservationEntity) {
        return new ReservationResponse(
                reservationEntity.getId(),
                reservationEntity.getUserId(),
                reservationEntity.getRoomId(),
                reservationEntity.getStartDate(),
                reservationEntity.getEndDate(),
                reservationEntity.getStatus());
    }

    public ReservationEntity toEntity(Long userId, CreateReservationRequest reservationRequest) {
        return new ReservationEntity(
                null,
                userId,
                reservationRequest.roomId(),
                reservationRequest.startDate(),
                reservationRequest.endDate(),
                ReservationStatus.PENDING);
    }
}
