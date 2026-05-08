package chainsawman.gesture.repository.user;

import chainsawman.gesture.entity.user.User;
import chainsawman.gesture.enums.ProviderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isDeactivated = false")
    Optional<User> findByEmailAndIsDeactivatedFalse(@Param("email") String email);

    Optional<User> findByIdxAndIsDeactivatedFalse(Long idx);
    Optional<User> findByEmail(String email);
    Optional<User> findByProviderAndProviderId(ProviderType provider, String providerId);


    // 유저 검색
    List<User> findByIdContainingIgnoreCaseAndIsDeactivatedFalse(String id);
    boolean existsByEmail(String email);
    boolean existsById(String id);
}
