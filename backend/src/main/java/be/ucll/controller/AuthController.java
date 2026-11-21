package be.ucll.controller;

import be.ucll.model.User;
import be.ucll.repository.UserRepository;
import be.ucll.security.JwtService;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthController(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users; this.encoder = encoder; this.jwt = jwt;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.getOrDefault("email", "").trim().toLowerCase();
        var user = users.findByEmailIgnoreCase(email).orElse(null);
        if (user == null || !user.isActive() || !encoder.matches(body.getOrDefault("password", ""), user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Ongeldige e-mail of wachtwoord"));
        }
        String token = jwt.generateToken(user.getEmail(), user.getRole().name());
        return ResponseEntity.ok(Map.of("token", token, "user", user));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(java.security.Principal principal) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return users.findByEmailIgnoreCase(principal.getName())
                .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}
