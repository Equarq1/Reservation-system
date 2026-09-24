package com.edmin.reservation_system.rooms;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RoomRepository extends JpaRepository<RoomEntity, Long> {
    boolean existsByName(String name);

    @Query("select r from RoomEntity r where" +
            "(:name is null OR r.name = :name) and " +
            "(:type is null OR r.type = :type) and" +
            "(:capacity is null or r.capacity >= :capacity) and" +
            "(:status is null or r.status = :status)")
    Page<RoomEntity> searchAllByFilter(@Param("name") String name,
                                       @Param("type") RoomType type,
                                       @Param("capacity") Long capacity,
                                       @Param("status") RoomStatus status,
                                       Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RoomEntity r where (r.id = :id)")
    Optional<RoomEntity> findByIdWithLock(@Param("id") Long id);
}
