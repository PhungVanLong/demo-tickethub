package demo.ticket_app.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "seat_holds")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatHold {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "seat_id", nullable = false)
    private Long seatId;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "hold_token", nullable = false)
    private UUID holdToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SeatHoldStatus status;

    @Column(name = "held_at", nullable = false)
    private LocalDateTime heldAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "released_at")
    private LocalDateTime releasedAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "order_id")
    private UUID orderId;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (status == null) {
            status = SeatHoldStatus.HELD;
        }
        if (heldAt == null) {
            heldAt = LocalDateTime.now();
        }
    }
}
