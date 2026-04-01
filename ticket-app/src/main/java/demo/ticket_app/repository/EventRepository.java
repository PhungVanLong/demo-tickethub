package demo.ticket_app.repository;

import demo.ticket_app.entity.Event;
import demo.ticket_app.entity.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    
    List<Event> findByOrganizerId(UUID organizerId);
    
    List<Event> findByStatus(EventStatus status);
    
    List<Event> findByCity(String city);
    
    List<Event> findByStatusAndCity(EventStatus status, String city);
    
    @Query("SELECT e FROM Event e WHERE e.startTime >= :startTime AND e.startTime <= :endTime")
    List<Event> findByStartTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                       @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT COUNT(e) FROM Event e WHERE e.status = :status")
    long countByStatus(@Param("status") EventStatus status);
    
    @Query("SELECT COUNT(e) FROM Event e WHERE e.organizerId = :organizerId AND e.status = :status")
    long countByOrganizerIdAndStatus(@Param("organizerId") UUID organizerId, 
                                    @Param("status") EventStatus status);
    
    @Query("SELECT e FROM Event e WHERE e.title LIKE %:search% OR e.description LIKE %:search%")
    List<Event> findByTitleOrDescriptionContaining(@Param("search") String search);
}
