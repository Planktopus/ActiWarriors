package be.ucll.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import be.ucll.model.Notification;
import be.ucll.model.User;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByTrainerOrderByCreatedAtDesc(User trainer);
    List<Notification> findAllByOrderByCreatedAtDesc();
}
