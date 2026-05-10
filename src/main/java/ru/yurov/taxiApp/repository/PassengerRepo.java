package ru.yurov.taxiApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yurov.taxiApp.entity.Passenger;

import java.util.Optional;

public interface PassengerRepo extends JpaRepository<Passenger, Long> {
    Optional<Passenger> findByEmailAndPassword(String email, String password);
}
