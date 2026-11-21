package be.ucll.controller;

import be.ucll.model.User;
import be.ucll.repository.UserRepository;
import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserRestController {
    private final UserRepository users;
    private final PasswordEncoder encoder;

    public UserRestController(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TRAINER')")
    public List<User> all() { return users.findAll(); }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TRAINER')")
    public ResponseEntity<?> create(@RequestBody Map<String, String> body, Principal principal) {
        User creator = users.findByEmailIgnoreCase(principal.getName()).orElseThrow();
        User.Role requestedRole;
        try { requestedRole = User.Role.valueOf(body.getOrDefault("role", "STUDENT")); }
        catch (IllegalArgumentException ex) { return ResponseEntity.badRequest().body(Map.of("message", "Ongeldige rol")); }

        if (creator.getRole() == User.Role.TRAINER && requestedRole != User.Role.STUDENT) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Een trainer kan alleen studenten aanmaken"));
        }

        String email = body.getOrDefault("email", "").trim().toLowerCase();
        if (email.isBlank() || users.findByEmailIgnoreCase(email).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "E-mail is verplicht en moet uniek zijn"));
        }

        User user = new User();
        user.setFirstName(body.getOrDefault("firstName", ""));
        user.setLastName(body.getOrDefault("lastName", ""));
        user.setEmail(email);
        user.setPasswordHash(encoder.encode(body.getOrDefault("password", "")));
        user.setRole(requestedRole);
        user.setActive(true);
        if (body.getOrDefault("password", "").isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Tijdelijk wachtwoord is verplicht"));
        }
        return ResponseEntity.ok(users.save(user));
    }

    @PatchMapping("/{id}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> setActive(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        return users.findById(id).map(user -> {
            user.setActive(body.getOrDefault("active", true));
            return ResponseEntity.ok(users.save(user));
        }).orElse(ResponseEntity.notFound().build());
    }
}
