package chainsawman.gesture.repository.quick;

import chainsawman.gesture.entity.quick.QuickSlot;
import chainsawman.gesture.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface QuickSlotRepository extends JpaRepository<QuickSlot, Long> {

    long countByUserAndDeletedAtIsNull(User user);

    @Query("select q.order from QuickSlot q where q.user = :user and q.deletedAt is null")
    Set<Integer> findOrdersByUser(@Param("user") User user);

    List<QuickSlot> findByIdxInAndUserAndDeletedAtIsNull(Collection<Long> ids, User user);

    Optional<QuickSlot> findByIdxAndUserAndDeletedAtIsNull(Long idx, User user);

    List<QuickSlot> findByUserAndDeletedAtIsNullOrderByOrder(User user);
}
