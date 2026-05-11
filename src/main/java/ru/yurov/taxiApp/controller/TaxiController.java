package ru.yurov.taxiApp.controller;


import jakarta.servlet.http.HttpSession;
import ru.yurov.taxiApp.entity.Passenger;
import ru.yurov.taxiApp.entity.Driver;
import ru.yurov.taxiApp.repository.PassengerRepo;
import ru.yurov.taxiApp.repository.DriverRepo;
import ru.yurov.taxiApp.repository.TripRepo;
import ru.yurov.taxiApp.service.TripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class TaxiController {
    @Autowired
    private PassengerRepo pRepo;
    @Autowired
    private DriverRepo dRepo;
    @Autowired
    private TripRepo tRepo;
    @Autowired
    private TripService tripService;

    @GetMapping("/")
    public String root(HttpSession session) {
        String role = (String) session.getAttribute("userRole");
        if (role == null) return "redirect:/login";
        if ("ADMIN".equals(role)) return "redirect:/admin";
        return "redirect:/profile";
    }

    @GetMapping("/admin")
    public String adminPage(HttpSession session, Model model) {
        if (!"ADMIN".equals(session.getAttribute("userRole"))) return "redirect:/login";

        model.addAttribute("passengers", pRepo.findAll());
        model.addAttribute("drivers", dRepo.findAll());
        model.addAttribute("trips", tRepo.findAll());
        model.addAttribute("tripCount", tRepo.count());
        double avg = tRepo.findAll().stream().mapToDouble(t -> t.getPrice() != null ? t.getPrice() : 0).average().orElse(0);
        model.addAttribute("avgPrice", String.format("%.2f", avg));
        return "admin";
    }

    @PostMapping("/passengers")
    public String addPassenger(@RequestParam String name, @RequestParam String email, @RequestParam String phone) {
        Passenger p = new Passenger();
        p.setName(name);
        p.setEmail(email);
        p.setPhone(phone);
        pRepo.save(p);
        return "redirect:/";
    }

    @PostMapping("/drivers")
    public String addDriver(@RequestParam String name, @RequestParam String email, @RequestParam String phone, @RequestParam String license) {
        Driver d = new Driver();
        d.setName(name);
        d.setEmail(email);
        d.setPhone(phone);
        d.setLicenseNumber(license);
        d.setStatus(ru.yurov.taxiApp.entity.enums.DriverStatus.AVAILABLE);
        dRepo.save(d);
        return "redirect:/";
    }

    // 2. Добавляем создание поездки из профиля (берёт ID пассажира из сессии)
    @PostMapping("/trips")
    public String createTripFromProfile(@RequestParam String origin, @RequestParam String destination,
                                        @RequestParam Double distance, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        String userType = (String) session.getAttribute("userType");

        if (userId == null || !"PASSENGER".equals(userType)) return "redirect:/login";

        try {
            tripService.createTrip(userId, origin, destination, distance);
        } catch (RuntimeException e) {
            System.out.println("⚠️ Ошибка заказа: " + e.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/trips/{id}/complete")
    public String completeTrip(@PathVariable Long id) {
        try {
            tripService.completeTrip(id);
        } catch (RuntimeException e) {
            System.out.println("Ошибка завершения: " + e.getMessage());
        }
        return "redirect:/";
    }
}
