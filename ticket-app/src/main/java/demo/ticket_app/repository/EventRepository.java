package demo.ticket_app.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import demo.ticket_app.entity.Event;
import demo.ticket_app.entity.EventStatus;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT e FROM Event e WHERE e.status = demo.ticket_app.entity.EventStatus.PUBLISHED " +
        "AND (:category IS NULL OR LOWER(e.category) = :category) " +
        "AND (:city IS NULL OR LOWER(e.city) = :city) " +
        "AND (:featured IS NULL OR e.featured = :featured)")
    Page<Event> findPublishedEvents(@Param("category") String category,
                    @Param("city") String city,
                    @Param("featured") Boolean featured,
                    Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.status = demo.ticket_app.entity.EventStatus.PUBLISHED " +
            "AND (:category IS NULL OR LOWER(e.category) = :category) " +
            "AND (:city IS NULL OR LOWER(e.city) = :city) " +
            "AND (:featured IS NULL OR e.featured = :featured)")
    List<Event> findPublishedEvents(@Param("category") String category,
                                    @Param("city") String city,
                                    @Param("featured") Boolean featured);
    
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
