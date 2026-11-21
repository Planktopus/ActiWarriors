package be.ucll.repository;

import be.ucll.model.Enrollment;
import be.ucll.model.Notification;
import be.ucll.model.TrainingSession;
import be.ucll.model.User;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DbInitializer {
    @Bean
    public CommandLineRunner seedDatabase(UserRepository users, TrainingSessionRepository trainings,
            EnrollmentRepository enrollments, NotificationRepository notifications,
            PasswordEncoder encoder,
            @Value("${app.admin.email}") String email,
            @Value("${app.admin.password}") String password) {
        return args -> {
            if (users.count() == 0) {
                User admin = createUser("System", "Administrator", email.trim().toLowerCase(), password, User.Role.ADMIN, encoder);
                users.save(admin);
            }

            if (trainings.count() >= 36) return;

            String[] trainerNames = {"Emma Janssens", "Lars Peeters", "Noor Vermeulen"};
            List<User> trainers = new ArrayList<>();
            for (int i = 0; i < trainerNames.length; i++) {
                int trainerIndex = i;
                String[] name = trainerNames[trainerIndex].split(" ");
                User trainer = users.findByEmailIgnoreCase("trainer" + (trainerIndex + 1) + "@app.com")
                        .orElseGet(() -> users.save(createUser(name[0], name[1], "trainer" + (trainerIndex + 1) + "@app.com", password, User.Role.TRAINER, encoder)));
                trainers.add(trainer);
            }

            String[] firstNames = {"Liam", "Olivia", "Noah", "Emma", "Lucas", "Mila", "Finn", "Louise", "Arthur", "Ella", "Jules", "Nora", "Matis", "Amber", "Victor", "Lina", "Seppe", "Marie", "Wout", "Fien"};
            String[] lastNames = {"De Smet", "Verstraete", "Claes", "Maes", "Wouters", "Jacobs", "Mertens", "Dubois", "Lambert", "Hermans", "Dumont", "Simon", "Van Acker", "Goossens", "Pauwels", "Vandenberg", "De Vos", "Bakker", "Martens", "Smet"};
            List<User> students = new ArrayList<>();
            for (int i = 0; i < firstNames.length; i++) {
                int studentIndex = i;
                String lastName = lastNames[studentIndex];
                User student = users.findByEmailIgnoreCase("student" + (studentIndex + 1) + "@app.com")
                        .orElseGet(() -> users.save(createUser(firstNames[studentIndex], lastName,
                                "student" + (studentIndex + 1) + "@app.com", password, User.Role.STUDENT, encoder)));
                students.add(student);
            }

            String[] titles = {"Krachttraining", "Cardio", "Yoga", "Mobility", "HIIT", "Core & Stability", "Bootcamp", "Kickboksen", "Pilates", "Fitbox"};
            LocalTime[] starts = {LocalTime.of(8, 0), LocalTime.of(10, 0), LocalTime.of(12, 0), LocalTime.of(15, 30), LocalTime.of(18, 0), LocalTime.of(20, 0)};
            List<TrainingSession> allSessions = new ArrayList<>();

            for (int i = 0; i < 36; i++) {
                int month = i < 12 ? 10 : i < 24 ? 11 : 12;
                int day = i < 12 ? 2 + i * 2 : i < 24 ? 2 + (i - 12) * 2 : 2 + (i - 24) * 2;
                if (day > 28) day = 28;

                TrainingSession training = new TrainingSession();
                training.setTitle(titles[i % titles.length]);
                training.setDate(LocalDate.of(2026, month, day));
                training.setStartTime(starts[i % starts.length]);
                training.setEndTime(starts[i % starts.length].plusMinutes(60));
                training.setTrainer(trainers.get(i % trainers.size()));
                training.setMaxCapacity(i % 9 == 0 ? 8 : 6);
                allSessions.add(trainings.save(training));
            }

            for (int i = 0; i < allSessions.size(); i++) {
                TrainingSession training = allSessions.get(i);
                int count = i % 7 == 0 ? 6 : 2 + i % 5;
                for (int j = 0; j < count && j < students.size(); j++) {
                    User student = students.get((i * 3 + j) % students.size());
                    if (enrollments.findByTrainingAndStudent(training, student).isPresent()) continue;

                    Enrollment enrollment = new Enrollment();
                    enrollment.setTraining(training);
                    enrollment.setStudent(student);
                    if (training.getDate().isBefore(LocalDate.now())) {
                        int result = (i + j) % 10;
                        enrollment.setStatus(result < 7
                                ? Enrollment.AttendanceStatus.AANWEZIG
                                : result < 9 ? Enrollment.AttendanceStatus.AFWEZIG : Enrollment.AttendanceStatus.INGESCHREVEN);
                    } else {
                        enrollment.setStatus(Enrollment.AttendanceStatus.INGESCHREVEN);
                    }
                    enrollments.save(enrollment);

                    Notification notification = new Notification();
                    notification.setTrainer(training.getTrainer());
                    notification.setMessage(student.getFirstName() + " " + student.getLastName() + " is ingeschreven voor "
                            + training.getTitle() + " op " + training.getDate() + " " + training.getStartTime());
                    notification.setReadStatus((i + j) % 4 == 0);
                    notification.setCreatedAt(LocalDateTime.now().minusDays(i).minusHours(j));
                    notifications.save(notification);
                }
            }

            for (int i = 0; i < 8; i++) {
                int seedIndex = i;
                TrainingSession training = allSessions.get(seedIndex);
                User student = students.get((seedIndex * 5) % students.size());
                enrollments.findByTrainingAndStudent(training, student).ifPresent(enrollment -> {
                    enrollments.delete(enrollment);
                    Notification notification = new Notification();
                    notification.setTrainer(training.getTrainer());
                    notification.setMessage(student.getFirstName() + " " + student.getLastName() + " is uitgeschreven voor "
                            + training.getTitle() + " op " + training.getDate() + " " + training.getStartTime());
                    notification.setReadStatus(seedIndex % 3 == 0);
                    notification.setCreatedAt(LocalDateTime.now().minusHours(seedIndex + 1));
                    notifications.save(notification);
                });
            }
        };
    }

    private User createUser(String firstName, String lastName, String email, String password,
                            User.Role role, PasswordEncoder encoder) {
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email.trim().toLowerCase());
        user.setPasswordHash(encoder.encode(password));
        user.setRole(role);
        user.setActive(true);
        return user;
    }
}
