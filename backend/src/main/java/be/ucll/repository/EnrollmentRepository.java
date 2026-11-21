package be.ucll.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import be.ucll.model.Enrollment;
import be.ucll.model.TrainingSession;
import be.ucll.model.User;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByTraining(TrainingSession training);
    Optional<Enrollment> findByTrainingAndStudent(TrainingSession training, User student);
    long countByTraining(TrainingSession training);
    List<Enrollment> findByStudent(User student);
}
