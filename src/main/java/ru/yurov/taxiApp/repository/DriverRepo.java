package ru.yurov.taxiApp.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import ru.yurov.taxiApp.entity.Driver;
import ru.yurov.taxiApp.entity.enums.DriverStatus;

import java.util.Optional;

public interface DriverRepo extends JpaRepository<Driver, Long> {
    Optional<Driver> findByEmailAndPassword(String email, String password);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Driver> findFirstByStatus(DriverStatus status);
}
