package com.edmin.reservation_system.reservations;

import com.edmin.reservation_system.reservations.availability.ReservationAvailabilityService;
import com.edmin.reservation_system.users.CustomUserDetails;
import com.edmin.reservation_system.users.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;


import java.util.*;

@Service
public class ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);

    private final ReservationRepository repository;
    private final ReservationMapper mapper;
    private final ReservationAvailabilityService availabilityService;

    public ReservationService(ReservationRepository repository, ReservationMapper mapper, ReservationAvailabilityService availabilityService) {
        this.repository = repository;
        this.mapper = mapper;
        this.availabilityService = availabilityService;
    }

    public ReservationResponse getReservationById(Long id, CustomUserDetails userDetails) {
        ReservationEntity reservation = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Not found reservation by id = " + id));
        checkOwnershipOrAdmin(reservation, userDetails);
        return mapper.toDomain(reservation);
    }

    public List<ReservationResponse> searchAllByFilter(ReservationSearchFilter filter, CustomUserDetails userDetails) {
        boolean isAdmin = userDetails.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        Long targetUserId = isAdmin ? filter.userId() : userDetails.getId();
        int pageSize = filter.pageSize() != null ? filter.pageSize() : 10;
        int pageNumber = filter.pageNumber() != null ? filter.pageNumber() : 0;
        Pageable pageable  = Pageable.ofSize(pageSize).withPage(pageNumber);
        List<ReservationEntity> allEntities = repository.searchAllByFilter(
                filter.roomId(),
                targetUserId,
                pageable
        );
        return allEntities.stream().map(mapper::toDomain).toList();
    }

    public ReservationResponse createReservation(Long userId, CreateReservationRequest reservationToCreate) {

        if (!reservationToCreate.endDate().isAfter(reservationToCreate.startDate())) {
            throw new IllegalArgumentException("start date must be 1 day earlier than end date");
        }

        ReservationEntity reservationEntity = mapper.toEntity(userId, reservationToCreate);
        reservationEntity.setStatus(ReservationStatus.PENDING);

        ReservationEntity reservation = repository.save(reservationEntity);

        return mapper.toDomain(reservation);
    }

    public ReservationResponse updateReservation(Long id, UpdateReservationRequest reservationToUpdate, CustomUserDetails userDetails) {
        ReservationEntity reservationEntity = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Not found reservation by id = " + id));
        checkOwnershipOrAdmin(reservationEntity, userDetails);
        if (reservationEntity.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Cannot modify reservation");
        }

        if (!reservationToUpdate.endDate().isAfter(reservationToUpdate.startDate())) {
            throw new IllegalArgumentException("start date must be 1 day earlier than end date");
        }

        reservationEntity.setRoomId(reservationToUpdate.roomId());
        reservationEntity.setStartDate(reservationToUpdate.startDate());
        reservationEntity.setEndDate(reservationToUpdate.endDate());
        ReservationEntity updatedReservation = repository.save(reservationEntity);
        return mapper.toDomain(updatedReservation);
    }

    @Transactional
    public void cancelReservation(Long id, CustomUserDetails userDetails) {
        ReservationEntity reservation = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Not found reservation by id"));
        checkOwnershipOrAdmin(reservation, userDetails);
        if (reservation.getStatus().equals(ReservationStatus.APPROVED)) {
            throw new IllegalStateException("cannot cancel approved reservation");
        }

        if (reservation.getStatus().equals(ReservationStatus.CANCELED)) {
            throw new IllegalStateException("reservation was already canceled");
        }
        reservation.setStatus(ReservationStatus.CANCELED);
        log.info("Successfully canceled reservation: id = {}", id);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ReservationResponse approveReservation(Long id) {
        ReservationEntity reservationEntity = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Not found reservation by id = " + id));

        if (reservationEntity.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Cannot approve reservation, status = " + reservationEntity.getStatus());
        }
        boolean isAvailableToApprove = availabilityService.isReservationAvailable(reservationEntity.getRoomId(), reservationEntity.getStartDate(), reservationEntity.getEndDate());
        if (!isAvailableToApprove) {
            throw new IllegalStateException("Cannot approve reservation because of conflict");
        }

        reservationEntity.setStatus(ReservationStatus.APPROVED);
        ReservationEntity reservation = repository.save(reservationEntity);
        return mapper.toDomain(reservation);
    }

    private void checkOwnershipOrAdmin(ReservationEntity reservation, CustomUserDetails user) {
        boolean isAdmin = user.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !reservation.getUserId().equals(user.getId())) {
            throw new AccessDeniedException("You are not the owner of this reservation");
        }


    }
}

