package ru.yurov.taxiApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yurov.taxiApp.entity.Trip;

import java.util.List;

public interface TripRepo extends JpaRepository<Trip, Long> {
    List<Trip> findByPassengerId(Long passengerId);
    List<Trip> findByDriverId(Long driverId);
}
