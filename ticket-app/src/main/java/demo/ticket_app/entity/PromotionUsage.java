package demo.ticket_app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "promotion_usages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionUsage {
    
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @Column(name = "promotion_id", nullable = false)
    private Long promotionId;
    
    @Column(name = "order_item_id", nullable = false)
    private UUID orderItemId;
    
    @Column(name = "used_at", nullable = false)
    private LocalDateTime usedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", insertable = false, updatable = false)
    private TicketPromotion promotion;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", insertable = false, updatable = false)
    private OrderItem orderItem;
    
    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (usedAt == null) {
            usedAt = LocalDateTime.now();
        }
    }
}
