package demo.ticket_app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "voucher_usages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherUsage {
    
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @Column(name = "voucher_id", nullable = false)
    private UUID voucherId;
    
    @Column(name = "order_id", nullable = false)
    private UUID orderId;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "used_at", nullable = false)
    private LocalDateTime usedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id", insertable = false, updatable = false)
    private Voucher voucher;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private Order order;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
    
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
