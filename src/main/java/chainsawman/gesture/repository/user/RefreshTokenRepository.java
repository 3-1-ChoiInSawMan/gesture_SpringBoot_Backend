package chainsawman.gesture.repository.user;

import chainsawman.gesture.entity.user.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByUser_Idx(Long userIdx);
    Optional<RefreshToken> findByToken(String token);
}
