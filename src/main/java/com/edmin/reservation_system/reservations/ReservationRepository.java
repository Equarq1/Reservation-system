package com.edmin.reservation_system.reservations;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {

    // List<ReservationEntity> findAllByStatusIs  (ReservationStatus status);

    @Query("select r from ReservationEntity r where r.status = :status")
    List<ReservationEntity> findAllByStatusIs(ReservationStatus status);

    @Modifying
    @Query("update ReservationEntity r set r.status = :status where r.id = :id")
    void setStatus(@Param("id") Long id,
                   @Param("status") ReservationStatus reservationStatus);

    @Query("SELECT r.id from ReservationEntity r WHERE r.room.id = :roomId" +
            " AND :startDate < r.endDate" +
            " AND r.startDate < :endDate" +
            " AND r.status = :status")
    List<Long> findConflictReservationIds (@Param("roomId") Long roomId,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate,
                                           @Param("status") ReservationStatus status);

    @Query("select r from ReservationEntity r where (:roomId IS NULL OR r.room.id = :roomId)" +
            " and (:userId IS NULL OR r.user.id = :userId)")
    Page<ReservationEntity> searchAllByFilter(
            @Param("roomId") Long roomId,
            @Param("userId") Long userId,
            Pageable pageable
    );

}