package demo.ticket_app.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import demo.ticket_app.config.SecurityUtils;
import demo.ticket_app.dto.auth.CreateStaffAccountRequest;
import demo.ticket_app.dto.auth.CreateStaffAccountResponse;
import demo.ticket_app.dto.common.PageResponse;
import demo.ticket_app.dto.event.CreateEventRequest;
import demo.ticket_app.dto.event.DecideEventRequest;
import demo.ticket_app.dto.event.EventDetailResponse;
import demo.ticket_app.dto.event.EventListItemResponse;
import demo.ticket_app.entity.Event;
import demo.ticket_app.entity.EventStatus;
import demo.ticket_app.entity.User;
import demo.ticket_app.service.EventService;
import demo.ticket_app.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
public class EventController {

    private final EventService eventService;
    private final UserService userService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents() {
        List<Event> events = eventService.getAllEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/published")
    public ResponseEntity<PageResponse<EventListItemResponse>> getPublishedEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(defaultValue = "date_asc") String sort) {
        return ResponseEntity.ok(eventService.getPublishedEvents(page, size, category, city, featured, sort));
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
    public ResponseEntity<EventDetailResponse> getEventById(@PathVariable Long eventId) {
        UUID requesterId = null;
        try { requesterId = securityUtils.getCurrentUserId(); } catch (Exception ignored) {}
        return ResponseEntity.ok(eventService.getEventDetailById(eventId, requesterId));
    }

    @GetMapping("/categories")
    public ResponseEntity<Map<String, List<String>>> getEventCategories() {
        return ResponseEntity.ok(Map.of("categories", List.of(
                "Concert", "Festival", "Conference", "Comedy", "Sports", "Expo"
        )));
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
        UUID requesterId = securityUtils.getCurrentUserId();
        Event updatedEvent = eventService.updateEvent(eventId, eventDetails, requesterId);
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
        UUID requesterId = securityUtils.getCurrentUserId();
        eventService.deleteEvent(eventId, requesterId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{eventId}/staff")
    public ResponseEntity<CreateStaffAccountResponse> createStaffForEvent(
            @PathVariable Long eventId,
            @Valid @RequestBody CreateStaffAccountRequest request) {
        User organizer = securityUtils.getCurrentUser();
        eventService.validateOrganizerCanManageStaffForEvent(eventId, organizer.getId());
        CreateStaffAccountResponse created = userService.createStaffAccountByOrganizer(organizer, request);
        return ResponseEntity.ok(created);
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
