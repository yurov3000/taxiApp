package ru.yurov.taxiApp.service;

import ru.yurov.taxiApp.entity.*;
import ru.yurov.taxiApp.entity.enums.DriverStatus;
import ru.yurov.taxiApp.entity.enums.TaskStatus;
import ru.yurov.taxiApp.entity.enums.TripStatus;
import ru.yurov.taxiApp.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yurov.taxiApp.repository.DriverRepo;
import ru.yurov.taxiApp.repository.NotificationRepo;
import ru.yurov.taxiApp.repository.PassengerRepo;
import ru.yurov.taxiApp.repository.TripRepo;

@Service
public class TripService {
    @Autowired
    private PassengerRepo passengerRepo;
    @Autowired
    private DriverRepo driverRepo;
    @Autowired
    private TripRepo tripRepo;
    @Autowired
    private NotificationRepo notifRepo;

    private static final double TARIFF_PER_KM = 50.0;

    @Transactional
    public Trip createTrip(Long passengerId, String origin, String destination, Double distance) {
        // 1. Проверка пассажира
        passengerRepo.findById(passengerId)
                .orElseThrow(() -> new RuntimeException("Passenger not found"));

        // 2. Поиск свободного водителя (атомарно благодаря @Lock + @Transactional)
        Driver driver = driverRepo.findFirstByStatus(DriverStatus.AVAILABLE)
                .orElseThrow(() -> new RuntimeException("No available drivers"));

        driver.setStatus(DriverStatus.BUSY);
        driverRepo.save(driver);

        // 3. Создание поездки
        Trip trip = new Trip();
        trip.setPassengerId(passengerId);
        trip.setDriverId(driver.getId());
        trip.setOrigin(origin);
        trip.setDestination(destination);
        trip.setPrice(distance * TARIFF_PER_KM);
        trip.setStatus(TripStatus.ASSIGNED);
        trip = tripRepo.save(trip);

        // 4. Создание уведомлений
        createNotif(trip.getId(), "DRIVER", driver.getId(), "Новая поездка: " + origin + " -> " + destination);
        createNotif(trip.getId(), "PASSENGER", passengerId, "Водитель найден. Ожидание.");

        return trip;
    }

    @Transactional
    public void cancelTrip(Long tripId, Long userId, String userType) {
        Trip trip = tripRepo.findById(tripId).orElseThrow(() -> new RuntimeException("Поездка не найдена"));

        boolean isOwner = ("PASSENGER".equals(userType) && trip.getPassengerId().equals(userId)) ||
                ("DRIVER".equals(userType) && trip.getDriverId().equals(userId));
        if (!isOwner) throw new RuntimeException("Нет доступа к этой поездке");
        if (trip.getStatus() == TripStatus.COMPLETED || trip.getStatus() == TripStatus.CANCELLED)
            throw new RuntimeException("Поездку уже нельзя отменить");

        trip.setStatus(TripStatus.CANCELLED);
        tripRepo.save(trip);

        // Освобождаем водителя, если он был назначен
        if (trip.getDriverId() != null) {
            Driver driver = driverRepo.findById(trip.getDriverId()).orElseThrow();
            driver.setStatus(DriverStatus.AVAILABLE);
            driverRepo.save(driver);
        }
        createNotif(tripId, "PASSENGER", trip.getPassengerId(), "Поездка отменена.");
        if (trip.getDriverId() != null) createNotif(tripId, "DRIVER", trip.getDriverId(), "Поездка отменена.");
    }

    @Transactional
    public void completeTrip(Long tripId) {
        Trip trip = tripRepo.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Поездка не найдена"));

        if (trip.getStatus() != TripStatus.ASSIGNED) {
            throw new RuntimeException("Поездку можно завершить только из статуса ASSIGNED");
        }

        // 1. Завершаем поездку
        trip.setStatus(TripStatus.COMPLETED);
        tripRepo.save(trip);

        // 2. Освобождаем водителя
        Driver driver = driverRepo.findById(trip.getDriverId())
                .orElseThrow(() -> new RuntimeException("Водитель не найден"));
        driver.setStatus(DriverStatus.AVAILABLE);
        driverRepo.save(driver);

        // 3. Уведомления
        createNotif(tripId, "PASSENGER", trip.getPassengerId(), "Поездка завершена. Спасибо!");
        createNotif(tripId, "DRIVER", driver.getId(), "Поездка завершена. Вы снова свободны.");
    }

    private void createNotif(Long tripId, String type, Long recId, String msg) {
        NotificationTask t = new NotificationTask();
        t.setTripId(tripId);
        t.setRecipientType(type);
        t.setRecipientId(recId);
        t.setMessage(msg);
        t.setStatus(TaskStatus.PENDING);
        t.setAttempts(0);
        notifRepo.save(t);
    }
}
