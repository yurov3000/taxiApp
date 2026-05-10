package ru.yurov.taxiApp.service;
import ru.yurov.taxiApp.entity.*;
import ru.yurov.taxiApp.entity.enums.DriverStatus;
import ru.yurov.taxiApp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired private PassengerRepo pRepo;
    @Autowired private DriverRepo dRepo;

    public Passenger registerPassenger(String name, String email, String phone, String password) {
        return pRepo.save(new Passenger(name, email, phone, password));
    }
    public Driver registerDriver(String name, String email, String phone, String license, String password) {
        Driver d = new Driver(name, email, phone, license, password);
        d.setStatus(DriverStatus.AVAILABLE);
        return dRepo.save(d);
    }
    public Object login(String email, String password, String role) {
        if ("PASSENGER".equalsIgnoreCase(role))
            return pRepo.findByEmailAndPassword(email, password).orElse(null);
        return dRepo.findByEmailAndPassword(email, password).orElse(null);
    }
}
