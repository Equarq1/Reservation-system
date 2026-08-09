package com.edmin.reservation_system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/reservation")
public class ReservationController {

    private static final Logger log = LoggerFactory.getLogger(ReservationController.class);

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getReservationById(@PathVariable Long id) {
        log.info("called getReservation");
        try {
            return ResponseEntity.status(200).body(reservationService.getReservationById(id));
        }
        catch (NoSuchElementException e) {
            return ResponseEntity.status(404).build();
        }
    }

    @GetMapping()
    public ResponseEntity<List<Reservation>> getAllReservations() {
        log.info("called getAllReservations");
        return ResponseEntity.status(200).body(reservationService.findAllReservations());
    }

    @PostMapping()
    public ResponseEntity<Reservation> createReservation(@RequestBody Reservation reservationToCreate) {
        log.info("called createReservation");
        return ResponseEntity.status(201).body(reservationService.createReservation(reservationToCreate));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Reservation> updateReservation(@PathVariable("id") Long id, @RequestBody Reservation reservationToUpdate) {
        log.info("called updateReservation");
        return ResponseEntity.status(200).body(reservationService.updateReservation(id, reservationToUpdate));
    }

    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<Void> deleteReservation(@PathVariable("id") Long id) {
        log.info("called deleteReservation");
        try {
            reservationService.cancelReservation(id);
            return ResponseEntity.status(200).build();
        }
        catch (NoSuchElementException e) {
            return ResponseEntity.status(404).build();
        }
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Reservation> approveReservation(@PathVariable("id") Long id) {
        log.info("Called approveReservation: id={}", id);
        Reservation reservation = reservationService.approveReservation(id);
        return ResponseEntity.ok(reservation);

    }


}
