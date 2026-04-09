package chainsawman.gesture.repository;

import chainsawman.gesture.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
