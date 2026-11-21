package be.ucll.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import be.ucll.model.User;

public interface UserRepository  extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);
    User findByEmail(String email);
    
}
