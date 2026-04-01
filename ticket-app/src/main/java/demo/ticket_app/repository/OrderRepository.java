package demo.ticket_app.repository;

import demo.ticket_app.entity.Order;
import demo.ticket_app.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    
    List<Order> findByUserId(UUID userId);
    
    List<Order> findByOrderStatus(OrderStatus orderStatus);
    
    List<Order> findByUserIdAndOrderStatus(UUID userId, OrderStatus orderStatus);
    
    @Query("SELECT o FROM Order o WHERE o.createdAt >= :startTime AND o.createdAt <= :endTime")
    List<Order> findByCreatedAtBetween(@Param("startTime") LocalDateTime startTime, 
                                      @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderStatus = :status")
    long countByOrderStatus(@Param("status") OrderStatus status);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.userId = :userId AND o.orderStatus = :status")
    long countByUserIdAndOrderStatus(@Param("userId") UUID userId, 
                                    @Param("status") OrderStatus status);
    
    @Query("SELECT SUM(o.finalAmount) FROM Order o WHERE o.orderStatus = 'CONFIRMED' AND o.createdAt >= :startTime")
    Long calculateGMVSince(@Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderStatus = 'CONFIRMED' AND o.createdAt >= :startTime")
    long countConfirmedOrdersSince(@Param("startTime") LocalDateTime startTime);
}
