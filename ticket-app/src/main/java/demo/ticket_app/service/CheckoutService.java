package demo.ticket_app.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import demo.ticket_app.dto.checkout.CheckoutItemRequest;
import demo.ticket_app.dto.checkout.CheckoutQuoteRequest;
import demo.ticket_app.dto.checkout.CheckoutQuoteResponse;
import demo.ticket_app.dto.checkout.CheckoutTierResponse;
import demo.ticket_app.dto.checkout.CreateCheckoutOrderRequest;
import demo.ticket_app.dto.checkout.CreateCheckoutOrderResponse;
import demo.ticket_app.entity.DiscountType;
import demo.ticket_app.entity.Event;
import demo.ticket_app.entity.EventStatus;
import demo.ticket_app.entity.Order;
import demo.ticket_app.entity.OrderItem;
import demo.ticket_app.entity.OrderStatus;
import demo.ticket_app.entity.TicketTier;
import demo.ticket_app.entity.Voucher;
import demo.ticket_app.exception.ResourceNotFoundException;
import demo.ticket_app.repository.EventRepository;
import demo.ticket_app.repository.OrderItemRepository;
import demo.ticket_app.repository.OrderRepository;
import demo.ticket_app.repository.TicketTierRepository;
import demo.ticket_app.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private static final BigDecimal SERVICE_FEE_RATE = new BigDecimal("0.05");

    private final EventRepository eventRepository;
    private final TicketTierRepository ticketTierRepository;
    private final VoucherRepository voucherRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional(readOnly = true)
    public List<CheckoutTierResponse> getAvailableTiers(Long eventId) {
        validateEventIsPublished(eventId);
        return ticketTierRepository.findBySeatMapEventIdAndQuantityAvailableGreaterThan(eventId, 0)
                .stream()
                .map(tier -> new CheckoutTierResponse(
                        tier.getId(),
                        tier.getName(),
                        tier.getPrice(),
                        tier.getQuantityAvailable()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public CheckoutQuoteResponse quote(CheckoutQuoteRequest request) {
        Event event = validateEventIsPublished(request.eventId());
        PricingResult pricingResult = calculatePricing(
                request.eventId(),
                request.items(),
                request.voucherCode()
        );

        return new CheckoutQuoteResponse(
                pricingResult.subtotal(),
            pricingResult.serviceFee(),
                pricingResult.discount(),
                pricingResult.total(),
                "VND",
                event.getEndTime()
        );
    }

    @Transactional
        public CreateCheckoutOrderResponse createOrder(CreateCheckoutOrderRequest request, java.util.UUID currentUserId) {
        validateEventIsPublished(request.eventId());
        PricingResult pricingResult = calculatePricing(
                request.eventId(),
                request.items(),
                request.voucherCode()
        );

        Order order = Order.builder()
            .userId(currentUserId)
                .orderStatus(OrderStatus.PENDING)
                .totalAmount(pricingResult.subtotal())
                .discountAmount(pricingResult.discount())
                .finalAmount(pricingResult.total())
                .notes("Checkout order")
                .build();

        Order savedOrder = orderRepository.save(order);

        List<OrderItem> orderItems = buildOrderItems(savedOrder.getId(), pricingResult.items());
        orderItemRepository.saveAll(orderItems);

        for (PricingItem pricingItem : pricingResult.items()) {
            TicketTier tier = pricingItem.ticketTier();
            int remaining = tier.getQuantityAvailable() - pricingItem.requestedQuantity();
            tier.setQuantityAvailable(remaining);
            ticketTierRepository.save(tier);
        }

        return new CreateCheckoutOrderResponse(
                savedOrder.getId(),
                savedOrder.getOrderCode(),
                savedOrder.getOrderStatus(),
            savedOrder.getFinalAmount(),
            savedOrder.getCreatedAt()
        );
    }

    private Event validateEventIsPublished(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));
        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new ResourceNotFoundException("Event not found with id: " + eventId);
        }
        return event;
    }

    private PricingResult calculatePricing(Long eventId, List<CheckoutItemRequest> itemRequests, String voucherCode) {
        List<PricingItem> pricingItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CheckoutItemRequest itemRequest : itemRequests) {
            TicketTier tier = ticketTierRepository.findByIdAndSeatMapEventId(itemRequest.ticketTierId(), eventId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Ticket tier not found with id: " + itemRequest.ticketTierId()));

            if (tier.getQuantityAvailable() < itemRequest.quantity()) {
                throw new IllegalArgumentException("Not enough tickets for tier: " + tier.getId());
            }

            BigDecimal lineTotal = tier.getPrice().multiply(BigDecimal.valueOf(itemRequest.quantity()));
            subtotal = subtotal.add(lineTotal);
            pricingItems.add(new PricingItem(tier, itemRequest.quantity(), lineTotal));
        }

        BigDecimal discount = calculateDiscount(subtotal, voucherCode);
        BigDecimal serviceFee = subtotal.multiply(SERVICE_FEE_RATE).setScale(2, java.math.RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(serviceFee).subtract(discount).max(BigDecimal.ZERO);
        return new PricingResult(pricingItems, subtotal, serviceFee, discount, total);
    }

    private BigDecimal calculateDiscount(BigDecimal subtotal, String voucherCode) {
        if (voucherCode == null || voucherCode.isBlank()) {
            return BigDecimal.ZERO;
        }

        Optional<Voucher> voucherOptional = voucherRepository.findActiveVoucherByCode(
                voucherCode,
                LocalDateTime.now()
        );

        if (voucherOptional.isEmpty()) {
            throw new IllegalArgumentException("Voucher is invalid or expired");
        }

        Voucher voucher = voucherOptional.get();
        if (voucher.getMinOrderValue() != null && subtotal.compareTo(voucher.getMinOrderValue()) < 0) {
            throw new IllegalArgumentException("Order does not meet voucher minimum value");
        }

        if (voucher.getDiscountType() == DiscountType.PERCENTAGE) {
            return subtotal
                    .multiply(voucher.getDiscountValue())
                    .divide(BigDecimal.valueOf(100));
        }

        return voucher.getDiscountValue().min(subtotal);
    }

    private List<OrderItem> buildOrderItems(java.util.UUID orderId, List<PricingItem> items) {
        return items.stream()
                .map(item -> OrderItem.builder()
                        .orderId(orderId)
                        .ticketTierId(item.ticketTier().getId())
                        .quantity(item.requestedQuantity())
                        .unitPrice(item.ticketTier().getPrice())
                        .discountAmount(BigDecimal.ZERO)
                        .finalPrice(item.lineTotal())
                        .build())
                .toList();
    }

    private record PricingItem(TicketTier ticketTier, int requestedQuantity, BigDecimal lineTotal) {
    }

    private record PricingResult(List<PricingItem> items, BigDecimal subtotal, BigDecimal serviceFee, BigDecimal discount, BigDecimal total) {
    }
}
