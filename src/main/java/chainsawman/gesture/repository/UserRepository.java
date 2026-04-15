package chainsawman.gesture.repository;

import chainsawman.gesture.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailAndIsDeactivatedFalse(String email);
}
