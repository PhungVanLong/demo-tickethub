package demo.ticket_app.repository;

import demo.ticket_app.entity.EventApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventApprovalRepository extends JpaRepository<EventApproval, Long> {

    List<EventApproval> findByEventId(Long eventId);

    Optional<EventApproval> findTopByEventIdOrderByDecidedAtDesc(Long eventId);
}
