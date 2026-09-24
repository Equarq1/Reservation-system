package com.edmin.reservation_system.rooms;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Table(name = "rooms")
@Entity
public class RoomEntity {

    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private RoomType type;

    @Column(name = "capacity", nullable = false)
    private Long capacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RoomStatus status;


    public RoomEntity(Long id, String name, RoomType type, Long capacity, RoomStatus status) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.capacity = capacity;
        this.status = status;
    }

    public RoomEntity() {

    }

}
