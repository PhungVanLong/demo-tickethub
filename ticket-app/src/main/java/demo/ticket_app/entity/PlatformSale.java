package demo.ticket_app.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "platform_sales")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformSale {
    
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_platformsale_voucher"))
    private demo.ticket_app.entity.Voucher voucher;
    
    @Column(name = "name", nullable = false, length = 255)
    private String name;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "discount_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercentage;
    
    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;
    
    @Column(name = "valid_until", nullable = false)
    private LocalDateTime validUntil;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (isActive == null) {
            isActive = true;
        }
    }
}
