package demo.ticket_app.service;

import demo.ticket_app.dto.tickettier.CreateTicketTierRequest;
import demo.ticket_app.entity.Event;
import demo.ticket_app.entity.SeatMap;
import demo.ticket_app.entity.TicketTier;
import demo.ticket_app.exception.ResourceNotFoundException;
import demo.ticket_app.repository.EventRepository;
import demo.ticket_app.repository.SeatMapRepository;
import demo.ticket_app.repository.TicketTierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TicketTierService {

    private final TicketTierRepository ticketTierRepository;
    private final SeatMapRepository seatMapRepository;
    private final EventRepository eventRepository;

    @Transactional(readOnly = true)
    public List<TicketTier> getByEventAndSeatMap(Long eventId, Long seatMapId) {
        validateOwnership(eventId, seatMapId);
        return ticketTierRepository.findBySeatMapId(seatMapId);
    }

    public TicketTier create(Long eventId, Long seatMapId, CreateTicketTierRequest request, UUID organizerId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));
        if (!event.getOrganizerId().equals(organizerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the organizer of this event");
        }
        validateOwnership(eventId, seatMapId);

        TicketTier tier = TicketTier.builder()
                .seatMapId(seatMapId)
                .name(request.name())
                .tierType(request.tierType())
                .price(request.price())
                .quantityTotal(request.quantityTotal())
                .quantityAvailable(request.quantityTotal())
                .colorCode(request.colorCode())
                .saleStart(request.saleStart())
                .saleEnd(request.saleEnd())
                .build();

        return ticketTierRepository.save(tier);
    }

    public void delete(Long eventId, Long seatMapId, Long tierId, UUID organizerId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));
        if (!event.getOrganizerId().equals(organizerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the organizer of this event");
        }
        validateOwnership(eventId, seatMapId);
        TicketTier tier = ticketTierRepository.findById(tierId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket tier not found with id: " + tierId));
        ticketTierRepository.delete(tier);
    }

    private void validateOwnership(Long eventId, Long seatMapId) {
        SeatMap seatMap = seatMapRepository.findById(seatMapId)
                .orElseThrow(() -> new ResourceNotFoundException("Seat map not found with id: " + seatMapId));
        if (!seatMap.getEventId().equals(eventId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seat map does not belong to this event");
        }
    }
}
