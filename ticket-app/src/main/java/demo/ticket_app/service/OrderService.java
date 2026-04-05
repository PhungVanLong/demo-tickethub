package demo.ticket_app.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import demo.ticket_app.entity.Order;
import demo.ticket_app.entity.OrderStatus;
import demo.ticket_app.entity.UserRole;
import demo.ticket_app.exception.ResourceNotFoundException;
import demo.ticket_app.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
    }

    public Order getOrderByIdForRequester(UUID orderId, UUID requesterId, UserRole requesterRole) {
        Order order = getOrderById(orderId);
        boolean isAdmin = requesterRole == UserRole.ADMIN;
        boolean isOwner = order.getUserId().equals(requesterId);

        if (!isAdmin && !isOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to view this order");
        }

        return order;
    }

    public List<Order> getOrdersByUserId(UUID userId) {
        return orderRepository.findByUserId(userId);
    }

    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByOrderStatus(status);
    }

    public List<Order> getOrdersByUserAndStatus(UUID userId, OrderStatus status) {
        return orderRepository.findByUserIdAndOrderStatus(userId, status);
    }

    public Order createOrder(Order order) {
        order.setOrderStatus(OrderStatus.PENDING);
        order.setOrderCode(generateOrderCode());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        
        Order savedOrder = orderRepository.save(order);
        log.info("Created new order with id: {}", savedOrder.getId());
        return savedOrder;
    }

    public Order updateOrder(UUID orderId, Order orderDetails) {
        Order existingOrder = getOrderById(orderId);
        
        existingOrder.setFinalAmount(orderDetails.getFinalAmount());
        existingOrder.setDiscountAmount(orderDetails.getDiscountAmount());
        existingOrder.setUpdatedAt(LocalDateTime.now());
        
        Order updatedOrder = orderRepository.save(existingOrder);
        log.info("Updated order with id: {}", updatedOrder.getId());
        return updatedOrder;
    }

    public Order confirmOrder(UUID orderId) {
        Order order = getOrderById(orderId);
        order.setOrderStatus(OrderStatus.CONFIRMED);
        order.setUpdatedAt(LocalDateTime.now());
        
        Order confirmedOrder = orderRepository.save(order);
        log.info("Confirmed order with id: {}", orderId);
        return confirmedOrder;
    }

    public Order cancelOrder(UUID orderId) {
        Order order = getOrderById(orderId);
        order.setOrderStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        
        Order cancelledOrder = orderRepository.save(order);
        log.info("Cancelled order with id: {}", orderId);
        return cancelledOrder;
    }

    public Order refundOrder(UUID orderId) {
        Order order = getOrderById(orderId);
        order.setOrderStatus(OrderStatus.REFUNDED);
        order.setUpdatedAt(LocalDateTime.now());
        
        Order refundedOrder = orderRepository.save(order);
        log.info("Refunded order with id: {}", orderId);
        return refundedOrder;
    }

    public void deleteOrder(UUID orderId) {
        Order order = getOrderById(orderId);
        orderRepository.delete(order);
        log.info("Deleted order with id: {}", orderId);
    }

    public List<Order> getOrdersByDateRange(LocalDateTime startTime, LocalDateTime endTime) {
        return orderRepository.findByCreatedAtBetween(startTime, endTime);
    }

    public long getOrdersByStatusCount(OrderStatus status) {
        return orderRepository.countByOrderStatus(status);
    }

    public long getOrdersByUserAndStatusCount(UUID userId, OrderStatus status) {
        return orderRepository.countByUserIdAndOrderStatus(userId, status);
    }

    public Long calculateGMVSince(LocalDateTime startTime) {
        return orderRepository.calculateGMVSince(startTime);
    }

    public long countConfirmedOrdersSince(LocalDateTime startTime) {
        return orderRepository.countConfirmedOrdersSince(startTime);
    }

    private String generateOrderCode() {
        return "ORD-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
    }
}
