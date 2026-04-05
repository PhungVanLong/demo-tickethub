package demo.ticket_app.service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import demo.ticket_app.dto.seatmap.HoldSeatsResponse;
import demo.ticket_app.dto.seatmap.SeatItemResponse;
import demo.ticket_app.entity.Seat;
import demo.ticket_app.entity.SeatHold;
import demo.ticket_app.entity.SeatHoldStatus;
import demo.ticket_app.entity.SeatMap;
import demo.ticket_app.entity.SeatStatus;
import demo.ticket_app.exception.ResourceNotFoundException;
import demo.ticket_app.repository.EventRepository;
import demo.ticket_app.repository.SeatHoldRepository;
import demo.ticket_app.repository.SeatMapRepository;
import demo.ticket_app.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SeatSelectionService {

    private static final long HOLD_MINUTES = 10;

    private final SeatRepository seatRepository;
    private final SeatMapRepository seatMapRepository;
    private final EventRepository eventRepository;
    private final SeatHoldRepository seatHoldRepository;

    @Transactional(readOnly = true)
    public List<SeatItemResponse> getSeats(Long eventId, Long seatMapId) {
        validateSeatMapOwnership(eventId, seatMapId);
        LocalDateTime now = LocalDateTime.now();

        List<Seat> seats = seatRepository.findBySeatMapIdOrderByRowLabelAscColNumberAsc(seatMapId);
        List<Long> seatIds = seats.stream().map(Seat::getId).toList();
        Map<Long, LocalDateTime> holdExpiresBySeatId = seatHoldRepository
                .findBySeatIdInAndStatusAndExpiresAtAfter(seatIds, SeatHoldStatus.HELD, now)
                .stream()
                .collect(Collectors.toMap(SeatHold::getSeatId, SeatHold::getExpiresAt, (left, right) -> left));

        return seats.stream()
                .map(seat -> toSeatItem(seat, holdExpiresBySeatId.get(seat.getId())))
                .toList();
    }

    public HoldSeatsResponse holdSeats(Long eventId, Long seatMapId, List<Long> seatIds, UUID userId) {
        validateSeatMapOwnership(eventId, seatMapId);
        if (seatIds == null || seatIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "seatIds is required");
        }

        expireOldHoldsInternal(LocalDateTime.now());

        List<Long> normalizedSeatIds = seatIds.stream().distinct().toList();
        List<Seat> seats = seatRepository.findByIdInForUpdate(normalizedSeatIds);
        if (seats.size() != normalizedSeatIds.size()) {
            throw new ResourceNotFoundException("One or more seats were not found");
        }

        for (Seat seat : seats) {
            if (!seatMapId.equals(seat.getSeatMapId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seat does not belong to this seat map");
            }
            if (seat.getStatus() != SeatStatus.AVAILABLE) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Seat is not available: " + seat.getSeatCode());
            }
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(HOLD_MINUTES);
        UUID holdToken = UUID.randomUUID();

        List<SeatHold> holds = seats.stream()
                .map(seat -> SeatHold.builder()
                        .seatId(seat.getId())
                        .eventId(eventId)
                        .userId(userId)
                        .holdToken(holdToken)
                        .status(SeatHoldStatus.HELD)
                        .heldAt(now)
                        .expiresAt(expiresAt)
                        .build())
                .toList();

        seats.forEach(seat -> seat.setStatus(SeatStatus.HELD));
        seatRepository.saveAll(seats);
        seatHoldRepository.saveAll(holds);

        List<SeatItemResponse> responseSeats = seats.stream()
                .map(seat -> toSeatItem(seat, expiresAt))
                .toList();

        return new HoldSeatsResponse(holdToken, expiresAt, responseSeats);
    }

    public void releaseHold(Long eventId, Long seatMapId, UUID holdToken, UUID userId) {
        validateSeatMapOwnership(eventId, seatMapId);

        List<SeatHold> holds = seatHoldRepository
                .findByHoldTokenAndUserIdAndEventIdAndStatus(holdToken, userId, eventId, SeatHoldStatus.HELD);
        if (holds.isEmpty()) {
            throw new ResourceNotFoundException("Active hold not found");
        }

        LocalDateTime now = LocalDateTime.now();
        List<Long> seatIds = holds.stream().map(SeatHold::getSeatId).distinct().toList();
        Map<Long, Seat> seatMap = seatRepository.findByIdInForUpdate(seatIds).stream()
                .collect(Collectors.toMap(Seat::getId, seat -> seat));

        for (SeatHold hold : holds) {
            Seat seat = seatMap.get(hold.getSeatId());
            if (seat != null && seatMapId.equals(seat.getSeatMapId()) && seat.getStatus() == SeatStatus.HELD) {
                seat.setStatus(SeatStatus.AVAILABLE);
            }
            hold.setStatus(SeatHoldStatus.RELEASED);
            hold.setReleasedAt(now);
        }

        seatRepository.saveAll(seatMap.values());
        seatHoldRepository.saveAll(holds);
    }

    public void confirmHold(Long eventId, Long seatMapId, UUID holdToken, UUID userId, UUID orderId) {
        validateSeatMapOwnership(eventId, seatMapId);

        List<SeatHold> holds = seatHoldRepository
                .findByHoldTokenAndUserIdAndEventIdAndStatus(holdToken, userId, eventId, SeatHoldStatus.HELD);
        if (holds.isEmpty()) {
            throw new ResourceNotFoundException("Active hold not found");
        }

        LocalDateTime now = LocalDateTime.now();
        if (holds.stream().anyMatch(h -> now.isAfter(h.getExpiresAt()))) {
            expireOldHoldsInternal(now);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Seat hold has expired");
        }

        List<Long> seatIds = holds.stream().map(SeatHold::getSeatId).distinct().toList();
        Map<Long, Seat> seatMap = seatRepository.findByIdInForUpdate(seatIds).stream()
                .collect(Collectors.toMap(Seat::getId, seat -> seat));

        for (SeatHold hold : holds) {
            Seat seat = seatMap.get(hold.getSeatId());
            if (seat == null || !seatMapId.equals(seat.getSeatMapId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seat does not belong to this seat map");
            }
            if (seat.getStatus() != SeatStatus.HELD) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Seat is no longer held: " + seat.getSeatCode());
            }
            seat.setStatus(SeatStatus.BOOKED);
            hold.setStatus(SeatHoldStatus.CONFIRMED);
            hold.setConfirmedAt(now);
            hold.setOrderId(orderId);
        }

        seatRepository.saveAll(seatMap.values());
        seatHoldRepository.saveAll(holds);
    }

    @Scheduled(cron = "0 */1 * * * *")
    public void expireOldHolds() {
        expireOldHoldsInternal(LocalDateTime.now());
    }

    private void expireOldHoldsInternal(LocalDateTime now) {
        List<SeatHold> expiredHolds = seatHoldRepository.findByStatusAndExpiresAtBefore(SeatHoldStatus.HELD, now);
        if (expiredHolds.isEmpty()) {
            return;
        }

        List<Long> seatIds = expiredHolds.stream().map(SeatHold::getSeatId).distinct().toList();
        Map<Long, Seat> seats = seatRepository.findByIdInForUpdate(seatIds).stream()
                .collect(Collectors.toMap(Seat::getId, seat -> seat, (left, right) -> left, LinkedHashMap::new));

        for (SeatHold hold : expiredHolds) {
            Seat seat = seats.get(hold.getSeatId());
            if (seat != null && seat.getStatus() == SeatStatus.HELD) {
                seat.setStatus(SeatStatus.AVAILABLE);
            }
            hold.setStatus(SeatHoldStatus.EXPIRED);
            hold.setReleasedAt(now);
        }

        seatRepository.saveAll(seats.values());
        seatHoldRepository.saveAll(expiredHolds);
        log.info("Expired {} seat holds", expiredHolds.size());
    }

    private SeatMap validateSeatMapOwnership(Long eventId, Long seatMapId) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Event not found with id: " + eventId);
        }

        SeatMap seatMap = seatMapRepository.findById(seatMapId)
                .orElseThrow(() -> new ResourceNotFoundException("Seat map not found with id: " + seatMapId));
        if (!eventId.equals(seatMap.getEventId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seat map does not belong to this event");
        }
        return seatMap;
    }

    private SeatItemResponse toSeatItem(Seat seat, LocalDateTime holdExpiresAt) {
        SeatStatus status = seat.getStatus();
        if (status == SeatStatus.HELD && holdExpiresAt == null) {
            status = SeatStatus.AVAILABLE;
        }
        return new SeatItemResponse(
                seat.getId(),
                seat.getSeatCode(),
                seat.getRowLabel(),
                seat.getColNumber(),
                seat.getTicketTierId(),
                status,
                holdExpiresAt
        );
    }
}
