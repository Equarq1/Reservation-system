package com.edmin.reservation_system.reservations;

import com.edmin.reservation_system.reservations.availability.ReservationAvailabilityService;
import com.edmin.reservation_system.rooms.RoomEntity;
import com.edmin.reservation_system.rooms.RoomRepository;
import com.edmin.reservation_system.rooms.RoomStatus;
import com.edmin.reservation_system.users.CustomUserDetails;
import com.edmin.reservation_system.users.UserEntity;
import com.edmin.reservation_system.users.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.util.*;

@Service
public class ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);

    private final ReservationRepository repository;
    private final ReservationMapper mapper;
    private final ReservationAvailabilityService availabilityService;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;

    public ReservationService(ReservationRepository repository, ReservationMapper mapper, ReservationAvailabilityService availabilityService, UserRepository userRepository, RoomRepository roomRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.availabilityService = availabilityService;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
    }

    @Transactional(readOnly = true)
    public ReservationResponse getReservationById(Long id, CustomUserDetails userDetails) {
        ReservationEntity reservation = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Not found reservation by id = " + id));
        checkOwnershipOrAdmin(reservation, userDetails);
        return mapper.toDomain(reservation);
    }

    @Transactional(readOnly = true)
    public Page<ReservationResponse> searchAllByFilter(ReservationSearchFilter filter, CustomUserDetails userDetails) {
        boolean isAdmin = userDetails.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        Long targetUserId = isAdmin ? filter.userId() : userDetails.getId();
        int pageSize = filter.pageSize() != null ? filter.pageSize() : 10;
        int pageNumber = filter.pageNumber() != null ? filter.pageNumber() : 0;
        Pageable pageable  = Pageable.ofSize(pageSize).withPage(pageNumber);
        Page<ReservationEntity> allEntities = repository.searchAllByFilter(
                filter.roomId(),
                targetUserId,
                pageable
        );
        return allEntities.map(mapper::toDomain);
    }

    @Transactional
    public ReservationResponse createReservation(Long userId, CreateReservationRequest reservationToCreate) {

        if (!reservationToCreate.startDate().isBefore(reservationToCreate.endDate())) {
            throw new IllegalArgumentException("start date must be 1 day earlier than end date");
        }

        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found with id = " + userId));
        RoomEntity room = roomRepository.findByIdWithLock(reservationToCreate.roomId()).orElseThrow(() -> new EntityNotFoundException("Room not found with id = " + reservationToCreate.roomId()));

        if (room.getStatus() != RoomStatus.AVAILABLE) {
            throw new IllegalStateException("Room status must be AVAILABLE");
        }

        boolean isAvailable = availabilityService.isReservationAvailable(
                room.getId(), reservationToCreate.startDate(), reservationToCreate.endDate());
        if (!isAvailable) {
            throw new IllegalStateException("Room is not available for selected dates");
        }

        ReservationEntity reservationEntity = mapper.toEntity(user, room, reservationToCreate);
        ReservationEntity response = repository.save(reservationEntity);
        return mapper.toDomain(response);
    }


    @Transactional
    public ReservationResponse updateReservation(Long id, UpdateReservationRequest reservationToUpdate, CustomUserDetails userDetails) {
        ReservationEntity reservationEntity = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Not found reservation with id = " + id));
        checkOwnershipOrAdmin(reservationEntity, userDetails);
        if (reservationEntity.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Cannot modify reservation");
        }

        if (!reservationToUpdate.startDate().isBefore(reservationToUpdate.endDate())) {
            throw new IllegalArgumentException("start date must be 1 day earlier than end date");
        }


        RoomEntity room = roomRepository.findByIdWithLock(reservationToUpdate.roomId()).orElseThrow(() -> new EntityNotFoundException("Not found room with id = " + reservationToUpdate.roomId()));

        if (room.getStatus() != RoomStatus.AVAILABLE) {
            throw new IllegalStateException("Room status must be AVAILABLE");
        }

        boolean isAvailable = availabilityService.isReservationAvailableForUpdate(
                room.getId(), reservationToUpdate.startDate(), reservationToUpdate.endDate(), id);
        if (!isAvailable) {
            throw new IllegalStateException("Room is already reserved for selected dates");
        }

        reservationEntity.setRoom(room);
        reservationEntity.setStartDate(reservationToUpdate.startDate());
        reservationEntity.setEndDate(reservationToUpdate.endDate());
        return mapper.toDomain(reservationEntity);
    }

    @Transactional
    public ReservationResponse cancelReservation(Long id, CustomUserDetails userDetails) {
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
        return mapper.toDomain(reservation);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ReservationResponse approveReservation(Long id) {
        ReservationEntity reservationEntity = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Not found reservation by id = " + id));

        if (reservationEntity.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Cannot approve reservation, status = " + reservationEntity.getStatus());
        }

        roomRepository.findByIdWithLock(reservationEntity.getRoom().getId()).orElseThrow(() -> new EntityNotFoundException("Room not found with id = " + reservationEntity.getRoom().getId()));

        boolean isAvailableToApprove = availabilityService.isReservationAvailable(reservationEntity.getRoom().getId(), reservationEntity.getStartDate(), reservationEntity.getEndDate());
        if (!isAvailableToApprove) {
            throw new IllegalStateException("Cannot approve reservation because of conflict");
        }

        reservationEntity.setStatus(ReservationStatus.APPROVED);
        return mapper.toDomain(reservationEntity);
    }

    private void checkOwnershipOrAdmin(ReservationEntity reservation, CustomUserDetails user) {
        boolean isAdmin = user.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !reservation.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not the owner of this reservation");
        }
    }

}

