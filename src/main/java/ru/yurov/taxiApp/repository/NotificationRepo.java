package ru.yurov.taxiApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yurov.taxiApp.entity.NotificationTask;
import ru.yurov.taxiApp.entity.enums.TaskStatus;

import java.util.Optional;

public interface NotificationRepo extends JpaRepository<NotificationTask, Long> {
    Optional<NotificationTask> findFirstByStatus(TaskStatus status);
}
