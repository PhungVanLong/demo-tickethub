package demo.ticket_app.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import demo.ticket_app.config.SecurityUtils;
import demo.ticket_app.dto.checkout.CheckoutQuoteRequest;
import demo.ticket_app.dto.checkout.CheckoutQuoteResponse;
import demo.ticket_app.dto.checkout.CheckoutTierResponse;
import demo.ticket_app.dto.checkout.CreateCheckoutOrderRequest;
import demo.ticket_app.dto.checkout.CreateCheckoutOrderResponse;
import demo.ticket_app.dto.payment.CreatePaymentIntentRequest;
import demo.ticket_app.dto.payment.CreatePaymentIntentResponse;
import demo.ticket_app.dto.payment.FakePaymentWebhookRequest;
import demo.ticket_app.dto.payment.FakePaymentWebhookResponse;
import demo.ticket_app.service.CheckoutService;
import demo.ticket_app.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class CheckoutController {

    private final CheckoutService checkoutService;
    private final PaymentService paymentService;
    private final SecurityUtils securityUtils;

    @GetMapping("/events/{eventId}/tiers")
    public ResponseEntity<List<CheckoutTierResponse>> getAvailableTiers(@PathVariable Long eventId) {
        return ResponseEntity.ok(checkoutService.getAvailableTiers(eventId));
    }

    @PostMapping("/quote")
    public ResponseEntity<CheckoutQuoteResponse> quote(@Valid @RequestBody CheckoutQuoteRequest request) {
        UUID currentUserId = securityUtils.getCurrentUserId();
        CheckoutQuoteRequest sanitizedRequest = new CheckoutQuoteRequest(
                currentUserId,
                request.eventId(),
                request.items(),
                request.voucherCode()
        );
        return ResponseEntity.ok(checkoutService.quote(sanitizedRequest));
    }

    @PostMapping("/orders")
    public ResponseEntity<CreateCheckoutOrderResponse> createOrder(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody CreateCheckoutOrderRequest request) {
        UUID currentUserId = securityUtils.getCurrentUserId();
        CreateCheckoutOrderRequest sanitizedRequest = new CreateCheckoutOrderRequest(
            request.eventId(),
            request.items(),
            request.voucherCode()
        );
        return ResponseEntity.ok(checkoutService.createOrder(sanitizedRequest, currentUserId));
    }

    @PostMapping("/orders/{orderId}/payments")
    public ResponseEntity<CreatePaymentIntentResponse> createPaymentIntent(
            @PathVariable java.util.UUID orderId,
            @Valid @RequestBody CreatePaymentIntentRequest request) {
        return ResponseEntity.ok(paymentService.createPaymentIntent(orderId, request));
    }

    @PostMapping("/payments/webhook/fake")
    public ResponseEntity<FakePaymentWebhookResponse> fakePaymentWebhook(
            @Valid @RequestBody FakePaymentWebhookRequest request) {
        return ResponseEntity.ok(paymentService.handleFakeWebhook(request));
    }
}
