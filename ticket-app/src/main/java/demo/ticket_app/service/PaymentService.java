package demo.ticket_app.service;

import demo.ticket_app.dto.admin.AdminPaymentItemResponse;
import demo.ticket_app.dto.admin.AdminPayoutExecuteResponse;
import demo.ticket_app.dto.admin.AdminPayoutPreviewResponse;
import demo.ticket_app.dto.admin.AdminPayoutRequest;
import demo.ticket_app.dto.admin.AdminRevenueSummaryResponse;
import demo.ticket_app.dto.payment.CreatePaymentIntentRequest;
import demo.ticket_app.dto.payment.CreatePaymentIntentResponse;
import demo.ticket_app.dto.payment.FakePaymentWebhookRequest;
import demo.ticket_app.dto.payment.FakePaymentWebhookResponse;
import demo.ticket_app.entity.Order;
import demo.ticket_app.entity.OrderItem;
import demo.ticket_app.entity.OrderStatus;
import demo.ticket_app.entity.Payment;
import demo.ticket_app.entity.PaymentStatus;
import demo.ticket_app.entity.PayoutStatus;
import demo.ticket_app.entity.SeatMap;
import demo.ticket_app.entity.TicketTier;
import demo.ticket_app.exception.ResourceNotFoundException;
import demo.ticket_app.repository.EventRepository;
import demo.ticket_app.repository.OrderItemRepository;
import demo.ticket_app.repository.OrderRepository;
import demo.ticket_app.repository.PaymentRepository;
import demo.ticket_app.repository.SeatMapRepository;
import demo.ticket_app.repository.TicketTierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    @Value("${payment.platform-fee-rate:0.10}")
    private BigDecimal platformFeeRate;

    @Value("${payment.gateway-fee-rate:0.02}")
    private BigDecimal gatewayFeeRate;

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final TicketTierRepository ticketTierRepository;
    private final SeatMapRepository seatMapRepository;
    private final EventRepository eventRepository;

    public CreatePaymentIntentResponse createPaymentIntent(UUID orderId, CreatePaymentIntentRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only pending orders can be paid");
        }

        FeeBreakdown fee = calculateFee(order.getFinalAmount());
        UUID organizerId = resolveOrganizerId(orderId);

        Payment payment = Payment.builder()
                .orderId(orderId)
            .organizerId(organizerId)
                .paymentMethod(request.method())
                .amount(order.getFinalAmount())
            .platformFeeAmount(fee.platformFeeAmount())
            .gatewayFeeAmount(fee.gatewayFeeAmount())
            .organizerNetAmount(fee.organizerNetAmount())
            .payoutStatus(PayoutStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .gatewayResponse("PENDING_FAKE_PROVIDER")
                .build();

        Payment saved = paymentRepository.save(payment);
        String paymentUrl = "http://localhost:8081/api/checkout/payments/fake-gateway?paymentCode=" + saved.getPaymentCode();

        return new CreatePaymentIntentResponse(
                saved.getId(),
                saved.getPaymentCode(),
                saved.getPaymentStatus(),
                saved.getAmount(),
                saved.getPlatformFeeAmount(),
                saved.getGatewayFeeAmount(),
                saved.getOrganizerNetAmount(),
                paymentUrl
        );
    }

    public FakePaymentWebhookResponse handleFakeWebhook(FakePaymentWebhookRequest request) {
        Payment payment = paymentRepository.findByPaymentCode(request.paymentCode())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with code: " + request.paymentCode()));

        Order order = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + payment.getOrderId()));

        if (Boolean.TRUE.equals(request.success())) {
            payment.setPaymentStatus(PaymentStatus.SUCCESS);
            payment.setTransactionId(request.transactionId() != null ? request.transactionId() : "FAKE-" + System.currentTimeMillis());
            payment.setGatewayResponse(request.message() != null ? request.message() : "Payment success from fake provider");
            payment.setPaidAt(LocalDateTime.now());

            order.setOrderStatus(OrderStatus.CONFIRMED);
            order.setUpdatedAt(LocalDateTime.now());
        } else {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            payment.setGatewayResponse(request.message() != null ? request.message() : "Payment failed from fake provider");

            order.setOrderStatus(OrderStatus.CANCELLED);
            order.setUpdatedAt(LocalDateTime.now());
        }

        paymentRepository.save(payment);
        orderRepository.save(order);

        return new FakePaymentWebhookResponse(
                order.getId(),
                payment.getPaymentCode(),
                payment.getPaymentStatus(),
                order.getOrderStatus(),
                payment.getAmount(),
                payment.getPlatformFeeAmount(),
                payment.getGatewayFeeAmount(),
                payment.getOrganizerNetAmount(),
                payment.getGatewayResponse()
        );
    }

            @Transactional(readOnly = true)
            public AdminPayoutPreviewResponse getPayoutPreview(UUID organizerId, LocalDateTime from, LocalDateTime to) {
            BigDecimal totalNet = paymentRepository.sumPendingPayoutOrganizerNet(
                organizerId,
                PaymentStatus.SUCCESS,
                PayoutStatus.PENDING,
                from,
                to
            );
            long pendingCount = paymentRepository.findPendingPayoutPayments(
                organizerId,
                PaymentStatus.SUCCESS,
                PayoutStatus.PENDING,
                from,
                to
            ).size();

            return new AdminPayoutPreviewResponse(organizerId, from, to, pendingCount, totalNet);
            }

            public AdminPayoutExecuteResponse executePayout(UUID organizerId, AdminPayoutRequest request) {
            List<Payment> pendingPayments = paymentRepository.findPendingPayoutPayments(
                organizerId,
                PaymentStatus.SUCCESS,
                PayoutStatus.PENDING,
                request.from(),
                request.to()
            );

            if (pendingPayments.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No pending payments to payout");
            }

            String payoutRef = (request.payoutReference() == null || request.payoutReference().isBlank())
                ? "PAYOUT-" + System.currentTimeMillis()
                : request.payoutReference();
            LocalDateTime paidAt = LocalDateTime.now();

            BigDecimal totalPaid = BigDecimal.ZERO;
            for (Payment payment : pendingPayments) {
                payment.setPayoutStatus(PayoutStatus.PAID);
                payment.setPayoutReference(payoutRef);
                payment.setPayoutAt(paidAt);
                totalPaid = totalPaid.add(Objects.requireNonNullElse(payment.getOrganizerNetAmount(), BigDecimal.ZERO));
            }
            paymentRepository.saveAll(pendingPayments);

            return new AdminPayoutExecuteResponse(
                organizerId,
                pendingPayments.size(),
                totalPaid,
                payoutRef,
                paidAt,
                request.note()
            );
            }

            @Transactional(readOnly = true)
            public AdminRevenueSummaryResponse getRevenueSummary(LocalDateTime from, LocalDateTime to) {
            long successfulPayments = paymentRepository.countByPaymentStatusAndPaidAtBetween(PaymentStatus.SUCCESS, from, to);
            BigDecimal gmv = paymentRepository.sumAmountByStatusAndPaidAtBetween(PaymentStatus.SUCCESS, from, to);
            BigDecimal platformFee = paymentRepository.sumPlatformFeeByStatusAndPaidAtBetween(PaymentStatus.SUCCESS, from, to);
            BigDecimal gatewayFee = paymentRepository.sumGatewayFeeByStatusAndPaidAtBetween(PaymentStatus.SUCCESS, from, to);
            BigDecimal organizerNet = paymentRepository.sumOrganizerNetByStatusAndPaidAtBetween(PaymentStatus.SUCCESS, from, to);

            return new AdminRevenueSummaryResponse(
                from,
                to,
                successfulPayments,
                gmv,
                platformFee,
                gatewayFee,
                organizerNet
            );
            }

            @Transactional(readOnly = true)
            public List<AdminPaymentItemResponse> getPayments(LocalDateTime from, LocalDateTime to) {
            return paymentRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(from, to)
                .stream()
                .map(p -> new AdminPaymentItemResponse(
                    p.getId(),
                    p.getPaymentCode(),
                    p.getOrderId(),
                    p.getPaymentMethod(),
                    p.getPaymentStatus(),
                    p.getAmount(),
                    p.getPlatformFeeAmount(),
                    p.getGatewayFeeAmount(),
                    p.getOrganizerNetAmount(),
                    p.getTransactionId(),
                    p.getPaidAt(),
                    p.getCreatedAt()
                ))
                .toList();
            }

    private FeeBreakdown calculateFee(BigDecimal amount) {
        BigDecimal normalizedAmount = amount.setScale(2, RoundingMode.HALF_UP);
        BigDecimal platformFee = normalizedAmount.multiply(platformFeeRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal gatewayFee = normalizedAmount.multiply(gatewayFeeRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal organizerNet = normalizedAmount.subtract(platformFee).subtract(gatewayFee)
                .max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
        return new FeeBreakdown(platformFee, gatewayFee, organizerNet);
    }

    private UUID resolveOrganizerId(UUID orderId) {
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        if (items.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order has no items");
        }

        UUID organizerId = null;
        for (OrderItem item : items) {
            TicketTier tier = ticketTierRepository.findById(item.getTicketTierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ticket tier not found with id: " + item.getTicketTierId()));
            SeatMap seatMap = seatMapRepository.findById(tier.getSeatMapId())
                    .orElseThrow(() -> new ResourceNotFoundException("Seat map not found with id: " + tier.getSeatMapId()));
            UUID currentOrganizerId = eventRepository.findById(seatMap.getEventId())
                    .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + seatMap.getEventId()))
                    .getOrganizerId();

            if (organizerId == null) {
                organizerId = currentOrganizerId;
            } else if (!organizerId.equals(currentOrganizerId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order contains multiple organizers");
            }
        }

        return organizerId;
    }

    private record FeeBreakdown(BigDecimal platformFeeAmount, BigDecimal gatewayFeeAmount, BigDecimal organizerNetAmount) {
    }
}
