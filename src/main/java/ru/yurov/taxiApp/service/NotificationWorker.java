package ru.yurov.taxiApp.service;

import ru.yurov.taxiApp.entity.*;
import ru.yurov.taxiApp.entity.enums.TaskStatus;
import ru.yurov.taxiApp.repository.NotificationRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.Optional;

@Component
public class NotificationWorker {
    @Autowired private NotificationRepo repo;
    private volatile boolean running = true;

    @PostConstruct
    public void startPool() {
        int threads = 4; // 3-5 по ТЗ
        for (int i = 0; i < threads; i++) {
            Thread t = new Thread(this::runLoop, "NotifWorker-" + i);
            t.setDaemon(true);
            t.start();
        }
    }

    @PreDestroy
    public void gracefulShutdown() {
        running = false;
        System.out.println("Graceful shutdown: stopping workers...");
    }

    private void runLoop() {
        while (running) {
            try {
                Optional<NotificationTask> opt = repo.findFirstByStatus(TaskStatus.PENDING);
                if (opt.isPresent()) {
                    processTask(opt.get());
                }
                Thread.sleep(1000); // Пауза чтобы не долбить БД
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void processTask(NotificationTask task) {
        task.setStatus(TaskStatus.PROCESSING);
        repo.save(task);

        try {
            // Имитация отправки
            Thread.sleep(2000);
            System.out.println("[SENT] " + task.getMessage() + " -> " + task.getRecipientType());
            task.setStatus(TaskStatus.SENT);
        } catch (Exception e) {
            task.setStatus(TaskStatus.FAILED);
            task.setAttempts(task.getAttempts() + 1);
            if (task.getAttempts() >= 3) {
                System.out.println("[FAILED MAX RETRIES] " + task.getId());
            } else {
                task.setStatus(TaskStatus.PENDING); // Вернем в очередь для повтора
                System.out.println("[RETRY] Task " + task.getId() + " attempt " + task.getAttempts());
            }
        }
        repo.save(task);
    }
}
