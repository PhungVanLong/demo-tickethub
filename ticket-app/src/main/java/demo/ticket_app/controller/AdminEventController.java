package demo.ticket_app.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import demo.ticket_app.config.SecurityUtils;
import demo.ticket_app.service.EventService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/events")
@RequiredArgsConstructor
public class AdminEventController {

    private final EventService eventService;
    private final SecurityUtils securityUtils;

    /**
     * Xóa sự kiện với quyền admin
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEventByAdmin(@PathVariable Long eventId) {
        UUID adminId = securityUtils.getCurrentUserId();
        eventService.deleteEvent(eventId, adminId);
        return ResponseEntity.noContent().build();
    }
}
