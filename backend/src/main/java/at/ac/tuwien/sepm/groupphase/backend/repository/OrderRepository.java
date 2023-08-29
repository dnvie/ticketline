package at.ac.tuwien.sepm.groupphase.backend.repository;

import at.ac.tuwien.sepm.groupphase.backend.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    /**
     * Find all orders entries for a user with a certain uuid.
     *
     * @return ordered list of all order entries matching the given uuid
     */
    @Query("SELECT o FROM Order o WHERE o.orderBy.id = :uid")
    List<Order> findOrdersByUuid(@Param("uid") UUID uid);

    @Query("SELECT DISTINCT o FROM Order o "
        + "LEFT JOIN FETCH o.tickets "
        + "WHERE EXISTS (SELECT 1 FROM o.tickets t WHERE t.forPerformance.startTime <= :deletionTime) "
        + "AND o.orderType = 1")
    List<Order> findOrdersByPerformanceTime(@Param("deletionTime") LocalDateTime deletionTime);

    @Query("SELECT o FROM Order o "
        + "WHERE o.orderBy.id = :uid "
        + "AND o.orderType = 0 "
        + "ORDER BY o.orderDate DESC")
    List<Order> findOrdersWithTypeBuyForUserAndOrderByOrderDate(@Param("uid") UUID uid);
}
