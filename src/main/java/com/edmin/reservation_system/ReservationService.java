package com.edmin.reservation_system;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);

    private final ReservationRepository repository;

    public ReservationService(ReservationRepository repository) {
        this.repository = repository;

    }

    public Reservation getReservationById(Long id) {
        ReservationEntity reservation = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Not found reservation by id = " + id));
        return toDomainReservation(reservation);
    }

    public List<Reservation> findAllReservations() {
        List<ReservationEntity> allEntities = repository.findAll();
        return allEntities.stream().map(this::toDomainReservation).toList();
    }

    public Reservation createReservation(Reservation reservationToCreate) {
        if (reservationToCreate.id() != null) {
            throw new IllegalArgumentException("Id should be empty");
        }
        if (reservationToCreate.status() != null) {
            throw new IllegalArgumentException("Status should be empty");
        }

        ReservationEntity reservationEntity = new ReservationEntity(
                null,
                reservationToCreate.userId(),
                reservationToCreate.roomId(),
                reservationToCreate.startDate(),
                reservationToCreate.endDate(),
                ReservationStatus.PENDING);

        ReservationEntity reservation = repository.save(reservationEntity);

        return toDomainReservation(reservation);
    }

    public Reservation updateReservation(Long id, Reservation reservationToUpdate) {
        ReservationEntity reservationEntity = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Not found reservation by id = " + id));
        if (reservationEntity.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Cannot modify reservation");
        }
        ReservationEntity updatedReservation = new ReservationEntity(
                reservationEntity.getId(),
                reservationToUpdate.userId(),
                reservationToUpdate.roomId(),
                reservationToUpdate.startDate(),
                reservationToUpdate.endDate(),
                reservationToUpdate.status());
        ReservationEntity reservation = repository.save(updatedReservation);
        return toDomainReservation(reservation);
    }

    @Transactional
    public void cancelReservation(Long id) {
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("No reservation by id = " + id);
        }
        repository.setStatus(id, ReservationStatus.CANCELED);
        log.info("Successfully canceled reservation: id = {}", id);
    }

    public Reservation approveReservation(Long id) {
        ReservationEntity reservationEntity = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Not found reservation by id = " + id));

        if (reservationEntity.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Cannot approve reservation, status = " + reservationEntity.getStatus());
        }
        boolean isConflict = isReservationConflict(reservationEntity);
        if (isConflict) {
            throw new IllegalStateException("Cannot approve reservation because of conflict");
        }

        reservationEntity.setStatus(ReservationStatus.APPROVED);
        ReservationEntity reservation = repository.save(reservationEntity);
        return toDomainReservation(reservation);
    }

    private boolean isReservationConflict (ReservationEntity reservation) {
        return repository.findAll().stream()
                .anyMatch(existing -> !reservation.getId().equals(existing.getId())
                        && reservation.getRoomId().equals(existing.getRoomId())
                        && existing.getStatus() == ReservationStatus.APPROVED
                        && reservation.getStartDate().isBefore(existing.getEndDate())
                        && existing.getStartDate().isBefore(reservation.getEndDate()));
    }

    private Reservation toDomainReservation(ReservationEntity reservationEntity) {
        return new Reservation(
                        reservationEntity.getId(),
                        reservationEntity.getUserId(),
                        reservationEntity.getRoomId(),
                        reservationEntity.getStartDate(),
                        reservationEntity.getEndDate(),
                        reservationEntity.getStatus());
    }
}
