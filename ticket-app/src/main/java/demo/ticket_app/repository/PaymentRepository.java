package demo.ticket_app.repository;

import demo.ticket_app.entity.Payment;
import demo.ticket_app.entity.PayoutStatus;
import demo.ticket_app.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByPaymentCode(String paymentCode);

    Optional<Payment> findTopByOrderIdOrderByCreatedAtDesc(UUID orderId);

    List<Payment> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime from, LocalDateTime to);

    long countByPaymentStatusAndPaidAtBetween(PaymentStatus status, LocalDateTime from, LocalDateTime to);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentStatus = :status AND p.paidAt BETWEEN :from AND :to")
    BigDecimal sumAmountByStatusAndPaidAtBetween(@Param("status") PaymentStatus status,
                                                 @Param("from") LocalDateTime from,
                                                 @Param("to") LocalDateTime to);

    @Query("SELECT COALESCE(SUM(p.platformFeeAmount), 0) FROM Payment p WHERE p.paymentStatus = :status AND p.paidAt BETWEEN :from AND :to")
    BigDecimal sumPlatformFeeByStatusAndPaidAtBetween(@Param("status") PaymentStatus status,
                                                      @Param("from") LocalDateTime from,
                                                      @Param("to") LocalDateTime to);

    @Query("SELECT COALESCE(SUM(p.gatewayFeeAmount), 0) FROM Payment p WHERE p.paymentStatus = :status AND p.paidAt BETWEEN :from AND :to")
    BigDecimal sumGatewayFeeByStatusAndPaidAtBetween(@Param("status") PaymentStatus status,
                                                     @Param("from") LocalDateTime from,
                                                     @Param("to") LocalDateTime to);

    @Query("SELECT COALESCE(SUM(p.organizerNetAmount), 0) FROM Payment p WHERE p.paymentStatus = :status AND p.paidAt BETWEEN :from AND :to")
    BigDecimal sumOrganizerNetByStatusAndPaidAtBetween(@Param("status") PaymentStatus status,
                                                       @Param("from") LocalDateTime from,
                                                       @Param("to") LocalDateTime to);

    @Query("SELECT p FROM Payment p WHERE p.organizerId = :organizerId AND p.paymentStatus = :paymentStatus AND p.payoutStatus = :payoutStatus AND p.paidAt BETWEEN :from AND :to ORDER BY p.paidAt ASC")
    List<Payment> findPendingPayoutPayments(@Param("organizerId") UUID organizerId,
                                            @Param("paymentStatus") PaymentStatus paymentStatus,
                                            @Param("payoutStatus") PayoutStatus payoutStatus,
                                            @Param("from") LocalDateTime from,
                                            @Param("to") LocalDateTime to);

    @Query("SELECT COALESCE(SUM(p.organizerNetAmount), 0) FROM Payment p WHERE p.organizerId = :organizerId AND p.paymentStatus = :paymentStatus AND p.payoutStatus = :payoutStatus AND p.paidAt BETWEEN :from AND :to")
    BigDecimal sumPendingPayoutOrganizerNet(@Param("organizerId") UUID organizerId,
                                            @Param("paymentStatus") PaymentStatus paymentStatus,
                                            @Param("payoutStatus") PayoutStatus payoutStatus,
                                            @Param("from") LocalDateTime from,
                                            @Param("to") LocalDateTime to);
}
