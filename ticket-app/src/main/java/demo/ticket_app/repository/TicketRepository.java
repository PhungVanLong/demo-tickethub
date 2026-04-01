package demo.ticket_app.repository;

import demo.ticket_app.entity.Ticket;
import demo.ticket_app.entity.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    
    @Query("SELECT t FROM Ticket t JOIN OrderItem oi ON t.orderItemId = oi.id JOIN Order o ON oi.orderId = o.id WHERE o.userId = :userId")
    List<Ticket> findByUserId(@Param("userId") UUID userId);
    
    List<Ticket> findByOrderItemId(UUID orderItemId);
    
    List<Ticket> findByTicketStatus(TicketStatus ticketStatus);
    
    @Query("SELECT t FROM Ticket t JOIN OrderItem oi ON t.orderItemId = oi.id JOIN Order o ON oi.orderId = o.id WHERE o.userId = :userId AND t.ticketStatus = :status")
    List<Ticket> findByUserIdAndTicketStatus(@Param("userId") UUID userId, 
                                            @Param("status") TicketStatus ticketStatus);
    
    @Query("SELECT t FROM Ticket t WHERE t.ticketCode = :ticketCode")
    Ticket findByTicketCode(@Param("ticketCode") String ticketCode);
    
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.ticketStatus = :status")
    long countByTicketStatus(@Param("status") TicketStatus status);
    
    @Query("SELECT COUNT(t) FROM Ticket t JOIN OrderItem oi ON t.orderItemId = oi.id JOIN Order o ON oi.orderId = o.id WHERE o.userId = :userId AND t.ticketStatus = :status")
    long countByUserIdAndTicketStatus(@Param("userId") UUID userId, 
                                    @Param("status") TicketStatus status);
    
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.ticketStatus = 'ACTIVE' AND t.usedAt >= :startTime")
    long countUsedTicketsSince(@Param("startTime") LocalDateTime startTime);
}
