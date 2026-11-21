package be.ucll.controller;

import be.ucll.model.*;
import be.ucll.repository.*;
import java.util.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final UserRepository users;
    private final EnrollmentRepository enrollments;

    public AdminController(UserRepository users, EnrollmentRepository enrollments) {
        this.users = users; this.enrollments = enrollments;
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Map<String,Object>> stats(@RequestParam int month, @RequestParam int year) {
        List<Map<String,Object>> result = new ArrayList<>();
        for (User student : users.findAll()) {
            if (student.getRole() != User.Role.STUDENT) continue;
            int enrolled = 0;
            int present = 0;
            for (Enrollment e : enrollments.findAll()) {
                if (!e.getStudent().getId().equals(student.getId())) continue;
                var date = e.getTraining().getDate();
                if (date.getMonthValue() == month && date.getYear() == year) {
                    enrolled++;
                    if (e.getStatus() == Enrollment.AttendanceStatus.AANWEZIG) present++;
                }
            }
            double percentage = enrolled == 0 ? 0 : Math.round(present * 1000.0 / enrolled) / 10.0;
            result.add(Map.of("studentId", student.getId(), "name", student.getFirstName()+" "+student.getLastName(),
                    "enrolled", enrolled, "present", present, "percentage", percentage));
        }
        return result;
    }
}
