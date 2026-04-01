package demo.ticket_app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "vouchers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Voucher {
    
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @Column(name = "organizer_id")
    private UUID organizerId;
    
    @Column(name = "code", nullable = false, unique = true, length = 100)
    private String code;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    private DiscountType discountType;
    
    @Column(name = "discount_value", nullable = false, precision = 18, scale = 2)
    private BigDecimal discountValue;
    
    @Column(name = "min_order_value", precision = 18, scale = 2)
    private BigDecimal minOrderValue;
    
    @Column(name = "usage_limit")
    private Integer usageLimit;
    
    @Column(name = "used_count", nullable = false)
    private Integer usedCount;
    
    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;
    
    @Column(name = "valid_until", nullable = false)
    private LocalDateTime validUntil;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "apply_on", nullable = false)
    private VoucherApplyOn applyOn;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", insertable = false, updatable = false)
    private User organizer;
    
    @OneToMany(mappedBy = "voucher", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VoucherUsage> voucherUsages;
    
    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (usedCount == null) {
            usedCount = 0;
        }
        if (isActive == null) {
            isActive = true;
        }
    }
}
