package chainsawman.gesture.repository.user;

import chainsawman.gesture.entity.user.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
}
