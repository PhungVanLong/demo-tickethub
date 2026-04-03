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
import java.util.UUID;

@Entity
@Table(name = "payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "organizer_id")
    private UUID organizerId;
    
    @Column(name = "payment_code", nullable = false, unique = true, length = 50)
    private String paymentCode;
    
    @Column(name = "payment_method", nullable = false, length = 50)
    private String paymentMethod;
    
    @Column(name = "transaction_id", length = 100)
    private String transactionId;
    
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "platform_fee_amount", precision = 10, scale = 2)
    private BigDecimal platformFeeAmount;

    @Column(name = "gateway_fee_amount", precision = 10, scale = 2)
    private BigDecimal gatewayFeeAmount;

    @Column(name = "organizer_net_amount", precision = 10, scale = 2)
    private BigDecimal organizerNetAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payout_status", nullable = false, length = 20)
    private PayoutStatus payoutStatus;

    @Column(name = "payout_reference", length = 100)
    private String payoutReference;

    @Column(name = "payout_at")
    private LocalDateTime payoutAt;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;
    
    @Column(name = "gateway_response", length = 500)
    private String gatewayResponse;
    
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private Order order;
    
    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (paymentCode == null) {
            paymentCode = generatePaymentCode();
        }
        if (paymentStatus == null) {
            paymentStatus = PaymentStatus.PENDING;
        }
        if (payoutStatus == null) {
            payoutStatus = PayoutStatus.PENDING;
        }
    }
    
    private String generatePaymentCode() {
        return "PAY" + System.currentTimeMillis();
    }
}
