package com.edmin.reservation_system;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class ReservationService {

    private final Map<Long, Reservation> reservationMap = Map.of(1L, new Reservation(1L, 100L, 40L, LocalDate.now(), LocalDate.now().plusDays(5), ReservationStatus.APPROVED),
            2L, new Reservation(2L, 101L, 41L, LocalDate.now(), LocalDate.now().plusDays(5), ReservationStatus.APPROVED),
            3L, new Reservation(3L, 102L, 42L, LocalDate.now(), LocalDate.now().plusDays(5), ReservationStatus.APPROVED));

    public Reservation getReservationById(Long id) {
        if (!reservationMap.containsKey(id)) {
            throw new NoSuchElementException("Element doesn't exists");
        }
        return reservationMap.get(id);
    }

    public List<Reservation> findAllReservations() {
        return reservationMap.values().stream().toList();
    }
}
