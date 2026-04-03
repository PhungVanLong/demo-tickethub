package demo.ticket_app.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import demo.ticket_app.config.SecurityUtils;
import demo.ticket_app.entity.User;
import demo.ticket_app.entity.UserRole;
import demo.ticket_app.service.EventService;
import demo.ticket_app.service.OrderService;
import demo.ticket_app.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
public class ApiController {

    private final UserService userService;
    private final EventService eventService;
    private final OrderService orderService;
    private final SecurityUtils securityUtils;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("service", "TicketHub API");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        User currentUser = securityUtils.getCurrentUser();
        Map<String, Object> payload = new HashMap<>();
        payload.put("role", currentUser.getRole());
        payload.put("userId", currentUser.getId());
        payload.put("email", currentUser.getEmail());
        payload.put("fullName", currentUser.getFullName());
        payload.put("canCreateEvent", true);

        Map<String, Object> stats = switch (currentUser.getRole()) {
            case ADMIN -> buildPlatformStats();
            case ORGANIZER -> buildOrganizerStats(currentUser.getId());
            case CUSTOMER -> buildCustomerStats(currentUser.getId());
        };
        payload.put("stats", stats);

        return ResponseEntity.ok(payload);
    }

    @GetMapping("/stats/platform")
    public ResponseEntity<Map<String, Object>> getPlatformStats() {
        return ResponseEntity.ok(buildPlatformStats());
    }

    @GetMapping("/stats/organizer/{organizerId}")
    public ResponseEntity<Map<String, Object>> getOrganizerStats(@PathVariable UUID organizerId) {
        return ResponseEntity.ok(buildOrganizerStats(organizerId));
    }

    private Map<String, Object> buildPlatformStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userService.getTotalUsersCount());
        stats.put("activeUsers", userService.getActiveUsersCount());
        stats.put("organizerUsers", userService.getUsersByRoleCount(UserRole.ORGANIZER));
        stats.put("totalEvents", eventService.getTotalEventsCount());
        stats.put("publishedEvents", eventService.getPublishedEventsCount());
        stats.put("pendingEvents", eventService.getPendingEventsCount());
        stats.put("confirmedOrdersToday", orderService.countConfirmedOrdersSince(LocalDateTime.now().withHour(0).withMinute(0).withSecond(0)));
        stats.put("monthlyGMV", orderService.calculateGMVSince(LocalDateTime.now().minusMonths(1)));
        return stats;
    }

    private Map<String, Object> buildOrganizerStats(UUID organizerId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEvents", eventService.getEventsByOrganizerCount(organizerId));
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
            stats.put("averageRating", 4.8);
        } catch (Exception e) {
            stats.put("totalSoldTickets", 0);
            stats.put("totalTickets", 0);
            stats.put("averageRating", 0);
        }
        return stats;
    }

    private Map<String, Object> buildCustomerStats(UUID userId) {
        Map<String, Object> stats = new HashMap<>();
        var orders = orderService.getOrdersByUserId(userId);
        long totalOrders = orders.size();
        long confirmedOrders = orders.stream().filter(order -> order.getOrderStatus() == demo.ticket_app.entity.OrderStatus.CONFIRMED).count();
        long pendingOrders = orders.stream().filter(order -> order.getOrderStatus() == demo.ticket_app.entity.OrderStatus.PENDING).count();
        stats.put("totalOrders", totalOrders);
        stats.put("confirmedOrders", confirmedOrders);
        stats.put("pendingOrders", pendingOrders);
        stats.put("availablePublishedEvents", eventService.getPublishedEventsCount());
        return stats;
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
