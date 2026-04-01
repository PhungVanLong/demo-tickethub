package demo.ticket_app.service;

import demo.ticket_app.dto.event.CreateEventRequest;
import demo.ticket_app.entity.ApprovalDecision;
import demo.ticket_app.entity.Event;
import demo.ticket_app.entity.EventApproval;
import demo.ticket_app.entity.EventStatus;
import demo.ticket_app.exception.ResourceNotFoundException;
import demo.ticket_app.repository.EventApprovalRepository;
import demo.ticket_app.repository.EventRepository;
import demo.ticket_app.repository.TicketTierRepository;
import demo.ticket_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EventService {

    private final EventRepository eventRepository;
    private final TicketTierRepository ticketTierRepository;
    private final EventApprovalRepository eventApprovalRepository;
    private final UserRepository userRepository;

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public List<Event> getPublishedEvents() {
        return eventRepository.findByStatus(EventStatus.PUBLISHED);
    }

    public List<Event> getPendingEvents() {
        return eventRepository.findByStatus(EventStatus.PENDING);
    }

    public List<Event> getEventsByOrganizer(UUID organizerId) {
        return eventRepository.findByOrganizerId(organizerId);
    }

    public List<Event> getEventsByCity(String city) {
        return eventRepository.findByCity(city);
    }

    public List<Event> getEventsByCityAndStatus(String city, EventStatus status) {
        return eventRepository.findByStatusAndCity(status, city);
    }

    public Event getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));
    }

    public Event createEvent(CreateEventRequest request, UUID organizerId) {
        Event event = Event.builder()
                .organizerId(organizerId)
                .title(request.title())
                .description(request.description())
                .venue(request.venue())
                .city(request.city())
                .locationCoords(request.locationCoords())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .bannerUrl(request.bannerUrl())
                .status(EventStatus.PENDING)
                .isPublished(false)
                .build();

        Event savedEvent = eventRepository.save(event);
        log.info("Created new event with id: {}", savedEvent.getId());
        return savedEvent;
    }

    /** Legacy overload — kept for backward compatibility */
    public Event createEvent(Event event) {
        event.setStatus(EventStatus.PENDING);
        event.setIsPublished(false);
        event.setCreatedAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());
        Event savedEvent = eventRepository.save(event);
        log.info("Created new event with id: {}", savedEvent.getId());
        return savedEvent;
    }

    public Event updateEvent(Long eventId, Event eventDetails) {
        Event existingEvent = getEventById(eventId);
        
        existingEvent.setTitle(eventDetails.getTitle());
        existingEvent.setDescription(eventDetails.getDescription());
        existingEvent.setVenue(eventDetails.getVenue());
        existingEvent.setCity(eventDetails.getCity());
        existingEvent.setStartTime(eventDetails.getStartTime());
        existingEvent.setEndTime(eventDetails.getEndTime());
        existingEvent.setUpdatedAt(LocalDateTime.now());
        
        Event updatedEvent = eventRepository.save(existingEvent);
        log.info("Updated event with id: {}", updatedEvent.getId());
        return updatedEvent;
    }

    public Event approveEvent(Long eventId, UUID adminId, String reason) {
        Objects.requireNonNull(adminId, "adminId is required");
        Event event = getEventById(eventId);
        event.setStatus(EventStatus.PUBLISHED);
        event.setIsPublished(true);
        event.setUpdatedAt(LocalDateTime.now());

        userRepository.findById(event.getOrganizerId()).ifPresent(user -> {
            user.setRole(demo.ticket_app.entity.UserRole.ORGANIZER);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
        });

        eventApprovalRepository.save(EventApproval.builder()
                .eventId(eventId)
                .adminId(adminId)
                .decision(ApprovalDecision.APPROVED)
                .reason(reason)
                .decidedAt(LocalDateTime.now())
                .build());

        Event approvedEvent = eventRepository.save(event);
        log.info("Approved event with id: {}", eventId);
        return approvedEvent;
    }

    public Event rejectEvent(Long eventId, UUID adminId, String reason) {
        Objects.requireNonNull(adminId, "adminId is required");
        Event event = getEventById(eventId);
        event.setStatus(EventStatus.REJECTED);
        event.setIsPublished(false);
        event.setUpdatedAt(LocalDateTime.now());

        userRepository.findById(event.getOrganizerId()).ifPresent(user -> {
            user.setRole(demo.ticket_app.entity.UserRole.CUSTOMER);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
        });

        eventApprovalRepository.save(EventApproval.builder()
                .eventId(eventId)
                .adminId(adminId)
                .decision(ApprovalDecision.REJECTED)
                .reason(reason)
                .decidedAt(LocalDateTime.now())
                .build());

        Event rejectedEvent = eventRepository.save(event);
        log.info("Rejected event with id: {}", eventId);
        return rejectedEvent;
    }

    public void deleteEvent(Long eventId) {
        Event event = getEventById(eventId);
        eventRepository.delete(event);
        log.info("Deleted event with id: {}", eventId);
    }

    public List<Event> searchEvents(String searchTerm) {
        return eventRepository.findByTitleOrDescriptionContaining(searchTerm);
    }

    public long getTotalEventsCount() {
        return eventRepository.count();
    }

    public long getPublishedEventsCount() {
        return eventRepository.countByStatus(EventStatus.PUBLISHED);
    }

    public long getPendingEventsCount() {
        return eventRepository.countByStatus(EventStatus.PENDING);
    }

    public long getEventsByOrganizerCount(UUID organizerId) {
        return eventRepository.countByOrganizerIdAndStatus(organizerId, EventStatus.PUBLISHED);
    }

    public long getTotalSoldTicketsForEvent(Long eventId) {
        return ticketTierRepository.countSoldTicketsByEventId(eventId);
    }

    public long getTotalTicketsForEvent(Long eventId) {
        return ticketTierRepository.countTotalTicketsByEventId(eventId);
    }
}
