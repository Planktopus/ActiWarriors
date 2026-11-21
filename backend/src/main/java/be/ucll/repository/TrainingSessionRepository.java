package be.ucll.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import be.ucll.model.TrainingSession;

public interface TrainingSessionRepository extends JpaRepository<TrainingSession, Long> {
    List<TrainingSession> findByDateBetweenOrderByDateAscStartTimeAsc(LocalDate from, LocalDate to);
}
