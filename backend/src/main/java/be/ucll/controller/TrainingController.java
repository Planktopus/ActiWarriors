package be.ucll.controller;

import be.ucll.model.*;
import be.ucll.repository.*;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/training-sessions")
public class TrainingController {
    private final TrainingSessionRepository trainings;
    private final EnrollmentRepository enrollments;
    private final UserRepository users;
    private final NotificationRepository notifications;

    public TrainingController(TrainingSessionRepository trainings, EnrollmentRepository enrollments,
                              UserRepository users, NotificationRepository notifications) {
        this.trainings = trainings; this.enrollments = enrollments; this.users = users; this.notifications = notifications;
    }

    @GetMapping
    public List<Map<String, Object>> all(@RequestParam(required=false) String from,
                                         @RequestParam(required=false) String to,
                                         Principal principal) {
        LocalDate start = from == null ? LocalDate.now().minusDays(31) : LocalDate.parse(from);
        LocalDate end = to == null ? LocalDate.now().plusDays(31) : LocalDate.parse(to);
        User viewer = users.findByEmailIgnoreCase(principal.getName()).orElseThrow();
        List<Map<String,Object>> result = new ArrayList<>();
        for (TrainingSession training : trainings.findByDateBetweenOrderByDateAscStartTimeAsc(start, end)) {
            Map<String,Object> item = trainingMap(training);
            var mine = enrollments.findByTrainingAndStudent(training, viewer);
            item.put("subscribed", mine.isPresent());
            item.put("enrollmentCount", enrollments.countByTraining(training));
            result.add(item);
        }
        return result;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TRAINER')")
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body, Principal principal) {
        User creator = users.findByEmailIgnoreCase(principal.getName()).orElseThrow();
        User trainer = creator;
        if (creator.getRole() == User.Role.ADMIN && body.get("trainerId") != null) {
            trainer = users.findById(Long.valueOf(body.get("trainerId").toString())).orElse(null);
            if (trainer == null || trainer.getRole() != User.Role.TRAINER) return ResponseEntity.badRequest().body(Map.of("message", "Kies een geldige trainer"));
        }
        TrainingSession training = new TrainingSession();
        training.setTitle(body.get("title").toString());
        training.setDate(LocalDate.parse(body.get("date").toString()));
        training.setStartTime(LocalTime.parse(body.get("startTime").toString()));
        training.setEndTime(LocalTime.parse(body.get("endTime").toString()));
        training.setTrainer(trainer);
        training.setMaxCapacity(body.get("maxCapacity") == null ? 6 : Integer.parseInt(body.get("maxCapacity").toString()));
        if (training.getMaxCapacity() < 1) return ResponseEntity.badRequest().body(Map.of("message", "Capaciteit moet minstens 1 zijn"));
        return ResponseEntity.ok(trainingMap(trainings.save(training)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TRAINER')")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body, Principal principal) {
        User creator = users.findByEmailIgnoreCase(principal.getName()).orElseThrow();
        return trainings.findById(id).map(training -> {
            if (creator.getRole() == User.Role.TRAINER && !training.getTrainer().getId().equals(creator.getId()))
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Je kunt alleen je eigen trainingen beheren"));
            if (body.containsKey("title")) training.setTitle(body.get("title").toString());
            if (body.containsKey("date")) training.setDate(LocalDate.parse(body.get("date").toString()));
            if (body.containsKey("startTime")) training.setStartTime(LocalTime.parse(body.get("startTime").toString()));
            if (body.containsKey("endTime")) training.setEndTime(LocalTime.parse(body.get("endTime").toString()));
            if (body.containsKey("maxCapacity")) training.setMaxCapacity(Integer.parseInt(body.get("maxCapacity").toString()));
            if (creator.getRole() == User.Role.ADMIN && body.get("trainerId") != null) {
                users.findById(Long.valueOf(body.get("trainerId").toString())).ifPresent(training::setTrainer);
            }
            return ResponseEntity.ok(trainingMap(trainings.save(training)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TRAINER')")
    public ResponseEntity<?> delete(@PathVariable Long id, Principal principal) {
        User creator = users.findByEmailIgnoreCase(principal.getName()).orElseThrow();
        var found = trainings.findById(id);
        if (found.isEmpty()) return ResponseEntity.notFound().build();
        TrainingSession training = found.get();
        if (creator.getRole() == User.Role.TRAINER && !training.getTrainer().getId().equals(creator.getId()))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        trainings.delete(training); return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/enrollments")
    @PreAuthorize("hasAnyRole('ADMIN','TRAINER')")
    public ResponseEntity<?> enrollmentDetails(@PathVariable Long id) {
        var training = trainings.findById(id).orElse(null);
        if (training == null) return ResponseEntity.notFound().build();
        List<Map<String,Object>> rows = new ArrayList<>();
        for (Enrollment e : enrollments.findByTraining(training)) {
            rows.add(Map.of("id", e.getId(), "studentId", e.getStudent().getId(),
                    "name", e.getStudent().getFirstName() + " " + e.getStudent().getLastName(),
                    "status", e.getStatus().name()));
        }
        return ResponseEntity.ok(rows);
    }

    @PostMapping("/{id}/enroll")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> enroll(@PathVariable Long id, Principal principal) {
        User student = users.findByEmailIgnoreCase(principal.getName()).orElseThrow();
        TrainingSession training = trainings.findById(id).orElse(null);
        if (training == null) return ResponseEntity.notFound().build();
        if (enrollments.findByTrainingAndStudent(training, student).isPresent()) return ResponseEntity.badRequest().body(Map.of("message", "Je bent al ingeschreven"));
        if (enrollments.countByTraining(training) >= training.getMaxCapacity()) return ResponseEntity.badRequest().body(Map.of("message", "Deze training is vol"));
        Enrollment enrollment = new Enrollment(); enrollment.setTraining(training); enrollment.setStudent(student); enrollment.setStatus(Enrollment.AttendanceStatus.INGESCHREVEN);
        enrollments.save(enrollment);
        addNotification(training, student, "ingeschreven");
        return ResponseEntity.ok(Map.of("message", "Inschrijving geslaagd"));
    }

    @DeleteMapping("/{id}/enroll")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> unenroll(@PathVariable Long id, Principal principal) {
        User student = users.findByEmailIgnoreCase(principal.getName()).orElseThrow();
        TrainingSession training = trainings.findById(id).orElse(null);
        if (training == null) return ResponseEntity.notFound().build();
        var found = enrollments.findByTrainingAndStudent(training, student);
        if (found.isEmpty()) return ResponseEntity.badRequest().body(Map.of("message", "Je bent niet ingeschreven"));
        enrollments.delete(found.get()); addNotification(training, student, "uitgeschreven");
        return ResponseEntity.ok(Map.of("message", "Uitgeschreven"));
    }

    @PatchMapping("/{trainingId}/enrollments/{enrollmentId}/attendance")
    @PreAuthorize("hasAnyRole('ADMIN','TRAINER')")
    public ResponseEntity<?> attendance(@PathVariable Long trainingId, @PathVariable Long enrollmentId,
                                        @RequestBody Map<String,String> body) {
        return enrollments.findById(enrollmentId).map(enrollment -> {
            try {
                enrollment.setStatus(Enrollment.AttendanceStatus.valueOf(body.get("status")));
                return ResponseEntity.ok(enrollments.save(enrollment));
            } catch (Exception ex) { return ResponseEntity.badRequest().body(Map.of("message", "Ongeldige status")); }
        }).orElse(ResponseEntity.notFound().build());
    }

    private void addNotification(TrainingSession training, User student, String action) {
        Notification n = new Notification(); n.setTrainer(training.getTrainer());
        n.setMessage(student.getFirstName() + " " + student.getLastName() + " is " + action + " voor " + training.getTitle() + " op " + training.getDate() + " " + training.getStartTime());
        notifications.save(n);
    }

    private Map<String,Object> trainingMap(TrainingSession t) {
        Map<String,Object> map = new LinkedHashMap<>();
        map.put("id", t.getId()); map.put("title", t.getTitle()); map.put("date", t.getDate().toString());
        map.put("startTime", t.getStartTime().toString()); map.put("endTime", t.getEndTime().toString());
        map.put("trainerId", t.getTrainer().getId()); map.put("trainerName", t.getTrainer().getFirstName()+" "+t.getTrainer().getLastName());
        map.put("maxCapacity", t.getMaxCapacity());
        return map;
    }
}
