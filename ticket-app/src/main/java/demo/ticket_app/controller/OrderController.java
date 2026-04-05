package demo.ticket_app.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import demo.ticket_app.config.SecurityUtils;
import demo.ticket_app.dto.order.OrderResponse;
import demo.ticket_app.entity.Order;
import demo.ticket_app.entity.OrderStatus;
import demo.ticket_app.entity.User;
import demo.ticket_app.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
public class OrderController {

    private final OrderService orderService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable UUID orderId) {
        User currentUser = securityUtils.getCurrentUser();
        Order order = orderService.getOrderByIdForRequester(orderId, currentUser.getId(), currentUser.getRole());
        return ResponseEntity.ok(OrderResponse.from(order));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getOrdersByUserId(@PathVariable UUID userId) {
        List<Order> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/me")
    public ResponseEntity<List<Order>> getMyOrders() {
        UUID userId = securityUtils.getCurrentUserId();
        List<Order> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Order>> getOrdersByStatus(@PathVariable OrderStatus status) {
        List<Order> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<List<Order>> getOrdersByUserAndStatus(
            @PathVariable UUID userId,
            @PathVariable OrderStatus status) {
        List<Order> orders = orderService.getOrdersByUserAndStatus(userId, status);
        return ResponseEntity.ok(orders);
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        Order createdOrder = orderService.createOrder(order);
        return ResponseEntity.ok(createdOrder);
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<Order> updateOrder(
            @PathVariable UUID orderId,
            @RequestBody Order orderDetails) {
        Order updatedOrder = orderService.updateOrder(orderId, orderDetails);
        return ResponseEntity.ok(updatedOrder);
    }

    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<Order> confirmOrder(@PathVariable UUID orderId) {
        Order confirmedOrder = orderService.confirmOrder(orderId);
        return ResponseEntity.ok(confirmedOrder);
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Order> cancelOrder(@PathVariable UUID orderId) {
        Order cancelledOrder = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(cancelledOrder);
    }

    @PostMapping("/{orderId}/refund")
    public ResponseEntity<Order> refundOrder(@PathVariable UUID orderId) {
        Order refundedOrder = orderService.refundOrder(orderId);
        return ResponseEntity.ok(refundedOrder);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrder(@PathVariable UUID orderId) {
        orderService.deleteOrder(orderId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<Order>> getOrdersByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        List<Order> orders = orderService.getOrdersByDateRange(startTime, endTime);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<Order>> getRecentOrders() {
        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        List<Order> orders = orderService.getOrdersByDateRange(startTime, LocalDateTime.now());
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/stats/status/{status}")
    public ResponseEntity<Long> getOrdersByStatusCount(@PathVariable OrderStatus status) {
        long count = orderService.getOrdersByStatusCount(status);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/stats/user/{userId}/status/{status}")
    public ResponseEntity<Long> getOrdersByUserAndStatusCount(
            @PathVariable UUID userId,
            @PathVariable OrderStatus status) {
        long count = orderService.getOrdersByUserAndStatusCount(userId, status);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/stats/gmv")
    public ResponseEntity<Long> getGMV() {
        LocalDateTime startTime = LocalDateTime.now().minusMonths(1);
        Long gmv = orderService.calculateGMVSince(startTime);
        return ResponseEntity.ok(gmv != null ? gmv : 0L);
    }

    @GetMapping("/stats/confirmed-today")
    public ResponseEntity<Long> getConfirmedOrdersToday() {
        LocalDateTime startTime = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        long count = orderService.countConfirmedOrdersSince(startTime);
        return ResponseEntity.ok(count);
    }
}
