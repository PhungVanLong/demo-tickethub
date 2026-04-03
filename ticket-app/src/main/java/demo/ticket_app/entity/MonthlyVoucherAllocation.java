package demo.ticket_app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.UUID;

@Entity
@Table(name = "monthly_voucher_allocations", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "year_month"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyVoucherAllocation {
    
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "year_month", nullable = false, length = 7)
    private String yearMonth;  // Format: "2026-04"
    
    @Column(name = "quantity_allocated", nullable = false)
    private Integer quantityAllocated;
    
    @Column(name = "quantity_created", nullable = false)
    private Integer quantityCreated;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
    
    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (quantityCreated == null) {
            quantityCreated = 0;
        }
    }
}
