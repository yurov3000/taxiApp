package ru.yurov.taxiApp.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.yurov.taxiApp.entity.enums.DriverStatus;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class Driver {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NonNull
    private String name, email, phone, licenseNumber;
    @NonNull
    private String password;
    @Enumerated(EnumType.STRING)
    private DriverStatus status;
    private LocalDateTime createdAt = LocalDateTime.now();
}
