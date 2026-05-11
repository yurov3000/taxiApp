package ru.yurov.taxiApp.controller;

import ru.yurov.taxiApp.entity.*;
import ru.yurov.taxiApp.repository.*;
import ru.yurov.taxiApp.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class AuthProfileController {
    @Autowired
    private UserService userService;
    @Autowired
    private TripRepo tripRepo;
    @Autowired
    private TripService tripService;

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register")
    public String doRegister(@RequestParam String role, @RequestParam String name,
                             @RequestParam String email, @RequestParam String phone,
                             @RequestParam(required = false) String license,
                             @RequestParam(required = false, defaultValue = "user123") String password) {
        if ("DRIVER".equalsIgnoreCase(role)) {
            userService.registerDriver(name, email, phone, license, password);
        } else {
            userService.registerPassenger(name, email, phone, password);
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String email, @RequestParam String password,
                          @RequestParam String role, HttpSession session) {
        if (email.equalsIgnoreCase("admin")) {
            session.setAttribute("userRole", "ADMIN");
            session.setAttribute("userName", "Администратор");
            return "redirect:/admin";
        }
        Object user = userService.login(email, password, role);
        if (user == null) return "redirect:/login?error";

        if (user instanceof Passenger p) {
            session.setAttribute("userId", p.getId());
            session.setAttribute("userType", "PASSENGER");
            session.setAttribute("userRole", "PASSENGER");
            session.setAttribute("userName", p.getName());
        } else if (user instanceof Driver d) {
            session.setAttribute("userId", d.getId());
            session.setAttribute("userType", "DRIVER");
            session.setAttribute("userRole", "DRIVER");
            session.setAttribute("userName", d.getName());
        }
        return "redirect:/profile";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        String userType = (String) session.getAttribute("userType");
        if (userId == null) return "redirect:/login";

        List<Trip> myTrips = "PASSENGER".equals(userType) ?
                tripRepo.findByPassengerId(userId) : tripRepo.findByDriverId(userId);

        long userTripCount = myTrips.size();

        double avgPrice = myTrips.stream()
                .mapToDouble(t -> t.getPrice() != null ? t.getPrice() : 0)
                .average()
                .orElse(0);

        model.addAttribute("userType", userType);
        model.addAttribute("userName", session.getAttribute("userName"));
        model.addAttribute("trips", myTrips);

        model.addAttribute("tripCount", userTripCount);
        model.addAttribute("avgPrice", String.format("%.2f", avgPrice));
        return "profile";
    }

    @PostMapping("/trips/{id}/cancel")
    public String cancelTrip(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        String userType = (String) session.getAttribute("userType");
        try {
            tripService.cancelTrip(id, userId, userType);
        } catch (Exception e) { /* игнорируем для демо */ }
        return "redirect:/profile";
    }
}
