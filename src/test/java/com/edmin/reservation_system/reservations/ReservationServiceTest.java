package com.edmin.reservation_system.reservations;

import com.edmin.reservation_system.reservations.availability.ReservationAvailabilityService;
import com.edmin.reservation_system.rooms.*;
import com.edmin.reservation_system.users.CustomUserDetails;
import com.edmin.reservation_system.users.UserEntity;
import com.edmin.reservation_system.users.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository repository;
    @Mock
    private ReservationMapper mapper;
    @Mock
    private ReservationAvailabilityService availabilityService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private ReservationService reservationService;

    private UserEntity user;
    private RoomEntity room;
    private CustomUserDetails userDetails;
    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        user = new UserEntity("test@test.com", "password", "ROLE_USER");
        user.setId(1L);

        room = new RoomEntity(1L, "Room A", RoomType.MEETING_ROOM, 10L, RoomStatus.AVAILABLE);

        userDetails = new CustomUserDetails(
                1L, "test@test.com", "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        startDate = LocalDate.now().plusDays(1);
        endDate = LocalDate.now().plusDays(3);
    }

    @Test
    void createReservation_success() {
        CreateReservationRequest request = new CreateReservationRequest(1L, startDate, endDate);
        ReservationEntity entity = new ReservationEntity(null, user, room, startDate, endDate, ReservationStatus.PENDING);
        ReservationEntity saved = new ReservationEntity(1L, user, room, startDate, endDate, ReservationStatus.PENDING);
        ReservationResponse expected = new ReservationResponse(1L, 1L, 1L, startDate, endDate, ReservationStatus.PENDING);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roomRepository.findByIdWithLock(1L)).thenReturn(Optional.of(room));
        when(availabilityService.isReservationAvailable(anyLong(), any(), any())).thenReturn(true);
        when(mapper.toEntity(user, room, request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(saved);
        when(mapper.toDomain(saved)).thenReturn(expected);

        ReservationResponse result = reservationService.createReservation(1L, request);

        assertNotNull(result);
        assertEquals(ReservationStatus.PENDING, result.status());
        verify(repository).save(entity);
    }

    @Test
    void createReservation_roomNotAvailable_throwsException() {
        room.setStatus(RoomStatus.MAINTENANCE);
        CreateReservationRequest request = new CreateReservationRequest(1L, startDate, endDate);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roomRepository.findByIdWithLock(1L)).thenReturn(Optional.of(room));

        assertThrows(IllegalStateException.class,
                () -> reservationService.createReservation(1L, request));

        verify(repository, never()).save(any());
    }

    @Test
    void createReservation_dateConflict_throwsException() {
        CreateReservationRequest request = new CreateReservationRequest(1L, startDate, endDate);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roomRepository.findByIdWithLock(1L)).thenReturn(Optional.of(room));
        when(availabilityService.isReservationAvailable(anyLong(), any(), any())).thenReturn(false);

        assertThrows(IllegalStateException.class,
                () -> reservationService.createReservation(1L, request));

        verify(repository, never()).save(any());
    }

    @Test
    void createReservation_startDateAfterEndDate_throwsException() {
        CreateReservationRequest request = new CreateReservationRequest(
                1L, endDate, startDate); // startDate ПОСЛЕ endDate

        assertThrows(IllegalArgumentException.class,
                () -> reservationService.createReservation(1L, request));

        verify(repository, never()).save(any());
    }


    @Test
    void updateReservation_success() {
        ReservationEntity existing = new ReservationEntity(1L, user, room, startDate, endDate, ReservationStatus.PENDING);
        UpdateReservationRequest request = new UpdateReservationRequest(1L, startDate.plusDays(5), endDate.plusDays(5));
        ReservationResponse expected = new ReservationResponse(1L, 1L, 1L, startDate.plusDays(5), endDate.plusDays(5), ReservationStatus.PENDING);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(roomRepository.findByIdWithLock(1L)).thenReturn(Optional.of(room));
        when(availabilityService.isReservationAvailableForUpdate(anyLong(), any(), any(), eq(1L))).thenReturn(true);
        when(mapper.toDomain(existing)).thenReturn(expected);

        ReservationResponse result = reservationService.updateReservation(1L, request, userDetails);

        assertNotNull(result);
        verify(repository, never()).save(any());
    }

    @Test
    void updateReservation_notOwner_throwsAccessDenied() {
        CustomUserDetails anotherUser = new CustomUserDetails(
                99L, "other@test.com", "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        ReservationEntity existing = new ReservationEntity(1L, user, room, startDate, endDate, ReservationStatus.PENDING);
        UpdateReservationRequest request = new UpdateReservationRequest(1L, startDate, endDate);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        assertThrows(AccessDeniedException.class,
                () -> reservationService.updateReservation(1L, request, anotherUser));
    }

    @Test
    void updateReservation_approvedReservation_throwsException() {
        ReservationEntity approved = new ReservationEntity(1L, user, room, startDate, endDate, ReservationStatus.APPROVED);
        UpdateReservationRequest request = new UpdateReservationRequest(1L, startDate, endDate);

        when(repository.findById(1L)).thenReturn(Optional.of(approved));

        assertThrows(IllegalStateException.class,
                () -> reservationService.updateReservation(1L, request, userDetails));
    }

    @Test
    void cancelReservation_success() {
        ReservationEntity pending = new ReservationEntity(1L, user, room, startDate, endDate, ReservationStatus.PENDING);
        ReservationResponse expected = new ReservationResponse(1L, 1L, 1L, startDate, endDate, ReservationStatus.CANCELED);

        when(repository.findById(1L)).thenReturn(Optional.of(pending));
        when(mapper.toDomain(pending)).thenReturn(expected);

        ReservationResponse result = reservationService.cancelReservation(1L, userDetails);

        assertNotNull(result);
        assertEquals(ReservationStatus.CANCELED, result.status());
    }

    @Test
    void cancelReservation_approvedReservation_throwsException() {
        ReservationEntity approved = new ReservationEntity(1L, user, room, startDate, endDate, ReservationStatus.APPROVED);

        when(repository.findById(1L)).thenReturn(Optional.of(approved));

        assertThrows(IllegalStateException.class,
                () -> reservationService.cancelReservation(1L, userDetails));
    }

    @Test
    void cancelReservation_alreadyCanceled_throwsException() {
        ReservationEntity canceled = new ReservationEntity(1L, user, room, startDate, endDate, ReservationStatus.CANCELED);

        when(repository.findById(1L)).thenReturn(Optional.of(canceled));

        assertThrows(IllegalStateException.class,
                () -> reservationService.cancelReservation(1L, userDetails));
    }
}