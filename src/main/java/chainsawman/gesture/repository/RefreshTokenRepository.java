package chainsawman.gesture.repository;

import chainsawman.gesture.entity.user.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
}
