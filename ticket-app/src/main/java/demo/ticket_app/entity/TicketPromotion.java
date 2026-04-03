package demo.ticket_app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ticket_promotions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketPromotion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "ticket_tier_id", nullable = false)
    private Long ticketTierId;
    
    @Column(name = "promo_name", nullable = false, length = 100)
    private String promoName;
    
    @Column(name = "description", length = 255)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "promo_type", nullable = false)
    private PromoType promoType;
    
    @Column(name = "value", nullable = false, precision = 10, scale = 2)
    private BigDecimal value;
    
    @Column(name = "min_quantity")
    private Integer minQuantity;
    
    @Column(name = "usage_limit")
    private Integer usageLimit;
    
    @Column(name = "used_count", nullable = false)
    private Integer usedCount;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
    
    @Column(name = "start_date")
    private LocalDateTime startDate;
    
    @Column(name = "end_date")
    private LocalDateTime endDate;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_tier_id", insertable = false, updatable = false)
    private TicketTier ticketTier;
    
    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PromotionUsage> promotionUsages;
    
    @PrePersist
    protected void onCreate() {
        if (usedCount == null) {
            usedCount = 0;
        }
        if (isActive == null) {
            isActive = true;
        }
    }
}
