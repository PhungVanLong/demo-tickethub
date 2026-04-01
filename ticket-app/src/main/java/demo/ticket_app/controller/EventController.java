package demo.ticket_app.controller;

import demo.ticket_app.config.SecurityUtils;
import demo.ticket_app.dto.event.CreateEventRequest;
import demo.ticket_app.dto.event.DecideEventRequest;
import demo.ticket_app.entity.Event;
import demo.ticket_app.entity.EventStatus;
import demo.ticket_app.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
public class EventController {

    private final EventService eventService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents() {
        List<Event> events = eventService.getAllEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/published")
    public ResponseEntity<List<Event>> getPublishedEvents() {
        List<Event> events = eventService.getPublishedEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Event>> getPendingEvents() {
        List<Event> events = eventService.getPendingEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/organizer/{organizerId}")
    public ResponseEntity<List<Event>> getEventsByOrganizer(@PathVariable UUID organizerId) {
        List<Event> events = eventService.getEventsByOrganizer(organizerId);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<List<Event>> getEventsByCity(@PathVariable String city) {
        List<Event> events = eventService.getEventsByCity(city);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/city/{city}/status/{status}")
    public ResponseEntity<List<Event>> getEventsByCityAndStatus(
            @PathVariable String city,
            @PathVariable EventStatus status) {
        List<Event> events = eventService.getEventsByCityAndStatus(city, status);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<Event> getEventById(@PathVariable Long eventId) {
        Event event = eventService.getEventById(eventId);
        return ResponseEntity.ok(event);
    }

    @PostMapping
    public ResponseEntity<Event> createEvent(@Valid @RequestBody CreateEventRequest request) {
        UUID organizerId = securityUtils.getCurrentUserId();
        Event createdEvent = eventService.createEvent(request, organizerId);
        return ResponseEntity.ok(createdEvent);
    }

    @PutMapping("/{eventId}")
    public ResponseEntity<Event> updateEvent(
            @PathVariable Long eventId,
            @RequestBody Event eventDetails) {
        Event updatedEvent = eventService.updateEvent(eventId, eventDetails);
        return ResponseEntity.ok(updatedEvent);
    }

    @PostMapping("/{eventId}/approve")
    public ResponseEntity<Event> approveEvent(
            @PathVariable Long eventId,
            @RequestBody(required = false) DecideEventRequest body) {
        UUID adminId = securityUtils.getCurrentUserId();
        String reason = body != null ? body.reason() : null;
        Event approvedEvent = eventService.approveEvent(eventId, adminId, reason);
        return ResponseEntity.ok(approvedEvent);
    }

    @PostMapping("/{eventId}/reject")
    public ResponseEntity<Event> rejectEvent(
            @PathVariable Long eventId,
            @RequestBody(required = false) DecideEventRequest body) {
        UUID adminId = securityUtils.getCurrentUserId();
        String reason = body != null ? body.reason() : null;
        Event rejectedEvent = eventService.rejectEvent(eventId, adminId, reason);
        return ResponseEntity.ok(rejectedEvent);
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long eventId) {
        eventService.deleteEvent(eventId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<Event>> searchEvents(@RequestParam String term) {
        List<Event> events = eventService.searchEvents(term);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/stats/total")
    public ResponseEntity<Long> getTotalEventsCount() {
        long count = eventService.getTotalEventsCount();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/stats/published")
    public ResponseEntity<Long> getPublishedEventsCount() {
        long count = eventService.getPublishedEventsCount();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/stats/pending")
    public ResponseEntity<Long> getPendingEventsCount() {
        long count = eventService.getPendingEventsCount();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/stats/organizer/{organizerId}")
    public ResponseEntity<Long> getEventsByOrganizerCount(@PathVariable UUID organizerId) {
        long count = eventService.getEventsByOrganizerCount(organizerId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/{eventId}/stats/sold-tickets")
    public ResponseEntity<Long> getSoldTicketsCount(@PathVariable Long eventId) {
        long count = eventService.getTotalSoldTicketsForEvent(eventId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/{eventId}/stats/total-tickets")
    public ResponseEntity<Long> getTotalTicketsCount(@PathVariable Long eventId) {
        long count = eventService.getTotalTicketsForEvent(eventId);
        return ResponseEntity.ok(count);
    }
}
