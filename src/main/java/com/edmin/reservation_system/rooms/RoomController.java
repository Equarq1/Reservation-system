package com.edmin.reservation_system.rooms;

import com.edmin.reservation_system.users.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rooms")
public class RoomController {
    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping()
    public ResponseEntity<RoomResponse> createRoom(@RequestBody @Valid CreateRoomRequest roomRequest) {
        RoomResponse response = roomService.createRoom(roomRequest);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping()
    public ResponseEntity<Page<RoomResponse>> searchAllByFilter(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "type", required = false) RoomType type,
            @RequestParam(value = "capacity", required = false) Long capacity,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
            @AuthenticationPrincipal CustomUserDetails user) {
        RoomSearchFilter filter = new RoomSearchFilter(name, type, capacity, pageSize, pageNumber);
        Page<RoomResponse> response = roomService.searchAllByFilter(filter, user);
        return ResponseEntity.status(200).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoomResponse> updateRoom (
            @PathVariable Long id,
            @RequestBody @Valid UpdateRoomRequest request
    ) {
        RoomResponse response = roomService.updateRoom(id, request);
        return ResponseEntity.status(200).body(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<RoomResponse> updateStatus (
           @PathVariable Long id,
           @RequestBody @Valid UpdateStatusRequest request
    ) {
        RoomResponse response = roomService.updateStatus(id, request);
        return ResponseEntity.status(200).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom (
            @PathVariable Long id
    ) {
        roomService.deleteRoom(id);
        return ResponseEntity.status(204).build();
    }
}
