package be.ucll.model;

import jakarta.persistence.*;

@Entity
@Table(name = "enrollments", uniqueConstraints = @UniqueConstraint(columnNames = {"training_id", "student_id"}))
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "training_id")
    private TrainingSession training;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id")
    private User student;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status = AttendanceStatus.INGESCHREVEN;

    public enum AttendanceStatus { INGESCHREVEN, AANWEZIG, AFWEZIG }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public TrainingSession getTraining() { return training; }
    public void setTraining(TrainingSession training) { this.training = training; }
    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }
    public AttendanceStatus getStatus() { return status; }
    public void setStatus(AttendanceStatus status) { this.status = status; }
}
