package com.edmin.reservation_system.rooms;

import com.edmin.reservation_system.users.CustomUserDetails;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository repository;
    @Mock
    private RoomMapper mapper;

    @InjectMocks
    private RoomService roomService;

    private CustomUserDetails adminDetails;

    @BeforeEach
    void setUp() {
        adminDetails = new CustomUserDetails(
                1L, "admin@test.com", "password",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    void createRoom_success() {
        CreateRoomRequest request = new CreateRoomRequest("Room A", RoomType.MEETING_ROOM, 10L, RoomStatus.AVAILABLE);
        RoomEntity entity = new RoomEntity(null, "Room A", RoomType.MEETING_ROOM, 10L, RoomStatus.AVAILABLE);
        RoomEntity saved = new RoomEntity(1L, "Room A", RoomType.MEETING_ROOM, 10L, RoomStatus.AVAILABLE);
        RoomResponse expected = new RoomResponse(1L, "Room A", RoomType.MEETING_ROOM, 10L, RoomStatus.AVAILABLE);

        when(repository.existsByName("Room A")).thenReturn(false);
        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(saved);
        when(mapper.toDomain(saved)).thenReturn(expected);

        RoomResponse result = roomService.createRoom(request);

        assertNotNull(result);
        assertEquals("Room A", result.name());
        verify(repository).save(entity);
    }

    @Test
    void createRoom_duplicateName_throwsException() {
        CreateRoomRequest request = new CreateRoomRequest("Room A", RoomType.MEETING_ROOM, 10L, RoomStatus.AVAILABLE);

        when(repository.existsByName("Room A")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> roomService.createRoom(request));

        verify(repository, never()).save(any());
    }

    @Test
    void updateRoom_notFound_throwsException() {
        UpdateRoomRequest request = new UpdateRoomRequest("New Name", RoomType.CONFERENCE_ROOM, 20L);

        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> roomService.updateRoom(99L, request));
    }

    @Test
    void updateRoom_success() {
        RoomEntity existing = new RoomEntity(1L, "Old Name", RoomType.MEETING_ROOM, 10L, RoomStatus.AVAILABLE);
        UpdateRoomRequest request = new UpdateRoomRequest("New Name", RoomType.CONFERENCE_ROOM, 20L);
        RoomResponse expected = new RoomResponse(1L, "New Name", RoomType.CONFERENCE_ROOM, 20L, RoomStatus.AVAILABLE);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.existsByName("New Name")).thenReturn(false);
        when(mapper.toDomain(existing)).thenReturn(expected);

        RoomResponse result = roomService.updateRoom(1L, request);

        assertNotNull(result);
        assertEquals("New Name", result.name());
        assertEquals(RoomType.CONFERENCE_ROOM, result.type());
    }
}