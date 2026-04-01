package demo.ticket_app.controller;

import demo.ticket_app.service.EventService;
import demo.ticket_app.service.OrderService;
import demo.ticket_app.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
public class ApiController {

    private final UserService userService;
    private final EventService eventService;
    private final OrderService orderService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("service", "TicketHub API");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats/platform")
    public ResponseEntity<Map<String, Object>> getPlatformStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // User stats
        stats.put("totalUsers", userService.getTotalUsersCount());
        stats.put("activeUsers", userService.getActiveUsersCount());
        stats.put("organizerUsers", userService.getUsersByRoleCount(demo.ticket_app.entity.UserRole.ORGANIZER));
        
        // Event stats
        stats.put("totalEvents", eventService.getTotalEventsCount());
        stats.put("publishedEvents", eventService.getPublishedEventsCount());
        stats.put("pendingEvents", eventService.getPendingEventsCount());
        
        // Order stats
        stats.put("confirmedOrdersToday", orderService.countConfirmedOrdersSince(LocalDateTime.now().withHour(0).withMinute(0).withSecond(0)));
        stats.put("monthlyGMV", orderService.calculateGMVSince(LocalDateTime.now().minusMonths(1)));
        
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/stats/organizer/{organizerId}")
    public ResponseEntity<Map<String, Object>> getOrganizerStats(@PathVariable UUID organizerId) {
        Map<String, Object> stats = new HashMap<>();
        
        // Organizer's events
        stats.put("totalEvents", eventService.getEventsByOrganizerCount(organizerId));
        
        // If organizer has events, get ticket stats
        try {
            var events = eventService.getEventsByOrganizer(organizerId);
            long totalSoldTickets = 0;
            long totalTickets = 0;
            
            for (var event : events) {
                totalSoldTickets += eventService.getTotalSoldTicketsForEvent(event.getId());
                totalTickets += eventService.getTotalTicketsForEvent(event.getId());
            }
            
            stats.put("totalSoldTickets", totalSoldTickets);
            stats.put("totalTickets", totalTickets);
            stats.put("averageRating", 4.8); // Mock data
        } catch (Exception e) {
            stats.put("totalSoldTickets", 0);
            stats.put("totalTickets", 0);
            stats.put("averageRating", 0);
        }
        
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/docs")
    public ResponseEntity<Map<String, Object>> getApiDocs() {
        Map<String, Object> docs = new HashMap<>();
        
        docs.put("title", "TicketHub API Documentation");
        docs.put("version", "1.0.0");
        docs.put("baseUrl", "http://localhost:8081/api");
        
        Map<String, String> endpoints = new HashMap<>();
        
        // Events endpoints
        endpoints.put("GET /events", "Get all events");
        endpoints.put("GET /events/published", "Get published events");
        endpoints.put("GET /events/pending", "Get pending events");
        endpoints.put("GET /events/{id}", "Get event by ID");
        endpoints.put("POST /events", "Create new event");
        endpoints.put("PUT /events/{id}", "Update event");
        endpoints.put("POST /events/{id}/approve", "Approve event");
        endpoints.put("POST /events/{id}/reject", "Reject event");
        
        // Users endpoints
        endpoints.put("GET /users", "Get all users");
        endpoints.put("GET /users/{id}", "Get user by ID");
        endpoints.put("GET /users/role/{role}", "Get users by role");
        endpoints.put("POST /users", "Create new user");
        endpoints.put("PUT /users/{id}", "Update user");
        
        // Orders endpoints
        endpoints.put("GET /orders", "Get all orders");
        endpoints.put("GET /orders/{id}", "Get order by ID");
        endpoints.put("GET /orders/user/{userId}", "Get orders by user");
        endpoints.put("POST /orders", "Create new order");
        endpoints.put("POST /orders/{id}/confirm", "Confirm order");
        
        // Stats endpoints
        endpoints.put("GET /stats/platform", "Get platform statistics");
        endpoints.put("GET /stats/organizer/{id}", "Get organizer statistics");
        
        docs.put("endpoints", endpoints);
        
        return ResponseEntity.ok(docs);
    }
}
