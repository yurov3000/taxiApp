package ru.yurov.taxiApp.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import ru.yurov.taxiApp.entity.enums.TripStatus;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Trip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long passengerId, driverId;
    @Enumerated(EnumType.STRING)
    private TripStatus status;
    private String origin, destination;
    private Double price;
    private Integer rating;
}
