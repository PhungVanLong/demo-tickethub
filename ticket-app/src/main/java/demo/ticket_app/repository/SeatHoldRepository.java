package demo.ticket_app.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import demo.ticket_app.entity.SeatHold;
import demo.ticket_app.entity.SeatHoldStatus;

@Repository
public interface SeatHoldRepository extends JpaRepository<SeatHold, UUID> {

    List<SeatHold> findBySeatIdInAndStatusAndExpiresAtAfter(List<Long> seatIds, SeatHoldStatus status, LocalDateTime now);

    List<SeatHold> findByHoldTokenAndUserIdAndEventIdAndStatus(UUID holdToken, UUID userId, Long eventId, SeatHoldStatus status);

    List<SeatHold> findByStatusAndExpiresAtBefore(SeatHoldStatus status, LocalDateTime now);
}
