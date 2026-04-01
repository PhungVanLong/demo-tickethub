package demo.ticket_app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tickets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {
    
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @Column(name = "order_item_id", nullable = false)
    private UUID orderItemId;
    
    @Column(name = "ticket_code", nullable = false, unique = true, length = 50)
    private String ticketCode;
    
    @Column(name = "qr_code", length = 255)
    private String qrCode;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "ticket_status", nullable = false)
    private TicketStatus ticketStatus;
    
    @Column(name = "seat_number", length = 20)
    private String seatNumber;
    
    @Column(name = "used_at")
    private LocalDateTime usedAt;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", insertable = false, updatable = false)
    private OrderItem orderItem;
    
    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (ticketCode == null) {
            ticketCode = generateTicketCode();
        }
        if (ticketStatus == null) {
            ticketStatus = TicketStatus.ACTIVE;
        }
    }
    
    private String generateTicketCode() {
        return "TCK" + System.currentTimeMillis();
    }
}
