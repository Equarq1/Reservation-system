package com.edmin.reservation_system.rooms;

import com.edmin.reservation_system.users.CustomUserDetails;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class RoomService {
    private final RoomRepository repository;
    private final RoomMapper mapper;

    public RoomService(RoomRepository repository, RoomMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional()
    @PreAuthorize("hasRole('ADMIN')")
    public RoomResponse createRoom(CreateRoomRequest request) {
        if (repository.existsByName(request.name())) {
            throw new IllegalArgumentException("Room with name " + request.name() + " already exists");
        }

        RoomEntity roomEntity = mapper.toEntity(request);
        RoomEntity entity = repository.save(roomEntity);
        return mapper.toDomain(entity);
    }

    @Transactional(readOnly = true)
    public Page<RoomResponse> searchAllByFilter(RoomSearchFilter filter, CustomUserDetails user) {
        boolean isAdmin =  user != null && user.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        int pageSize = filter.pageSize() != null ? filter.pageSize() : 10;
        int pageNumber = filter.pageNumber() != null ? filter.pageNumber() : 0;
        Pageable pageable = Pageable.ofSize(pageSize).withPage(pageNumber);
        Page<RoomEntity> entities;
        if (isAdmin) {
            entities = repository.searchAllByFilter(
                filter.name(),
                filter.type(),
                filter.capacity(),
                null,
                pageable
            );
        }
        else {
            entities = repository.searchAllByFilter(
                filter.name(),
                filter.type(),
                filter.capacity(),
                RoomStatus.AVAILABLE,
                pageable
            );
        }
        return entities.map(mapper::toDomain);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public RoomResponse updateRoom(Long id, UpdateRoomRequest request) {
        RoomEntity roomToUpdate = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Room not found with id = " + id));

        if (
                !roomToUpdate.getName().equals(request.name()) &&
                repository.existsByName(request.name())
        ) {
            throw new IllegalArgumentException("Room with name '" + request.name() + "' already exists");
        }

        roomToUpdate.setName(request.name());
        roomToUpdate.setType(request.type());
        roomToUpdate.setCapacity(request.capacity());
        return mapper.toDomain(roomToUpdate);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public RoomResponse updateStatus(Long id, UpdateStatusRequest request) {
        RoomEntity entity = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Room not found with id = " + id));
        entity.setStatus(request.status());
        return mapper.toDomain(entity);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteRoom(Long id) {
        repository.deleteById(id);
    }

}
