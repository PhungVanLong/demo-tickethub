package demo.ticket_app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import demo.ticket_app.entity.TicketTier;
import demo.ticket_app.entity.TicketTierType;

@Repository
public interface TicketTierRepository extends JpaRepository<TicketTier, Long> {
    
    List<TicketTier> findBySeatMapId(Long seatMapId);

    List<TicketTier> findBySeatMapEventIdAndQuantityAvailableGreaterThan(Long eventId, Integer quantityAvailable);

    java.util.Optional<TicketTier> findByIdAndSeatMapEventId(Long id, Long eventId);
    
    List<TicketTier> findByTierType(TicketTierType tierType);
    
    @Query("SELECT t FROM TicketTier t WHERE t.seatMapId = :seatMapId AND t.quantityAvailable > 0")
    List<TicketTier> findAvailableTiersByEventId(@Param("seatMapId") Long seatMapId);
    
    @Query("SELECT COUNT(t) FROM TicketTier t WHERE t.seatMapId = :seatMapId")
    long countBySeatMapId(@Param("seatMapId") Long seatMapId);
    
    @Query("SELECT SUM(t.quantityTotal - t.quantityAvailable) FROM TicketTier t WHERE t.seatMapId = :seatMapId")
    long countSoldTicketsByEventId(@Param("seatMapId") Long seatMapId);
    
    @Query("SELECT SUM(t.quantityTotal) FROM TicketTier t WHERE t.seatMapId = :seatMapId")
    long countTotalTicketsByEventId(@Param("seatMapId") Long seatMapId);
}
