package ru.yurov.taxiApp.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import ru.yurov.taxiApp.entity.enums.TaskStatus;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long tripId;
    private String recipientType; // PASSENGER или DRIVER
    private Long recipientId;
    private String message;
    @Enumerated(EnumType.STRING)
    private TaskStatus status;
    private Integer attempts;
}
