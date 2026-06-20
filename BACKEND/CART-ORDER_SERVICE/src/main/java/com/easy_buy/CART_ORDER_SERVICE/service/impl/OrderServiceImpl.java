package com.easy_buy.CART_ORDER_SERVICE.service.impl;

import com.easy_buy.CART_ORDER_SERVICE.entity.*;
import com.easy_buy.CART_ORDER_SERVICE.exception.BusinessRuleException;
import com.easy_buy.CART_ORDER_SERVICE.exception.ExternalServiceException;
import com.easy_buy.CART_ORDER_SERVICE.exception.ResourceNotFoundException;
import com.easy_buy.CART_ORDER_SERVICE.client.InventoryFeignClient;
import com.easy_buy.CART_ORDER_SERVICE.payload.request.CheckoutRequest;
import com.easy_buy.CART_ORDER_SERVICE.payload.request.ReleaseStockRequest;
import com.easy_buy.CART_ORDER_SERVICE.payload.response.OrderItemResponse;
import com.easy_buy.CART_ORDER_SERVICE.payload.response.OrderResponse;
import com.easy_buy.CART_ORDER_SERVICE.producer.OrderEventPublisher;
import com.easy_buy.CART_ORDER_SERVICE.repository.CartRepository;
import com.easy_buy.CART_ORDER_SERVICE.repository.OrderRepository;
import com.easy_buy.CART_ORDER_SERVICE.service.OrderService;
import com.easy_buy.COMMON_SERVICE.events.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final InventoryFeignClient inventoryClient;


    private final OrderEventPublisher orderEventPublisher;


    @Override
    public OrderResponse checkout(UUID userId, CheckoutRequest request) {

        // Find active cart of user
        Cart cart = cartRepository.findByUserIdAndCartStatus(userId, CartStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Active cart not found for user: " + userId));

        // Check cart is empty or not
        if (cart.getCartItems().isEmpty()) {
            throw new BusinessRuleException("Cannot checkout an empty cart");
        }

        //Convert cart into order
        Order order = buildOrderFromCart(cart, request);
        Order savedOrder = orderRepository.save(order);
        cart.getCartItems().clear();
        cart.setCartTotal(BigDecimal.ZERO);
        cart.setCartStatus(CartStatus.CHECKED_OUT);
        cart.setCheckedOutAt(Instant.now());
        cartRepository.save(cart);

        // Publish order event to Kafka

        OrderEvent orderEvent = new OrderEvent();
        orderEvent.setOrderId(savedOrder.getOrderId());
        orderEvent.setUserId(savedOrder.getUserId());
        orderEvent.setStatus(savedOrder.getOrderStatus().name());
        orderEvent.setMessage("Order created successfully");
        orderEvent.setTotalAmount(savedOrder.getTotalAmount());
        orderEventPublisher.publishOrderEvent(orderEvent);

        return toResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(UUID orderId) {
        Order order = orderRepository
                .findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Order not found with id : "
                                        + orderId
                        )
                );

        return toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByNumber(String orderNumber) {
        Order order = orderRepository
                .findByOrderNumber(orderNumber)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found with order number : " + orderNumber));

        return toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserId(UUID userId) {
        return orderRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(UUID orderId) {
        Order order = orderRepository
                .findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id : " + orderId));
        // Check already canceled
        if (order.getOrderStatus() == OrderStatus.CANCELLED)
        {
            throw new BusinessRuleException("Order is already cancelled");
        }
        // Update status
        order.setOrderStatus(OrderStatus.CANCELLED);

        // Set canceled time
        order.setCancelledAt(Instant.now());
        Order savedOrder = orderRepository.save(order);

        // Release reserved stock for each order item
        savedOrder.getOrderItems().forEach(item ->
                releaseReservedStock(item.getProductId(), item.getQuantity())
        );

        return toResponse(savedOrder);
    }

    @Override
    public void releaseReservedStock(UUID productId, Integer quantity) {
        try {
            ReleaseStockRequest req = new ReleaseStockRequest();
            req.setQuantity(quantity);
            inventoryClient.releaseStock(productId, req);
        } catch (Exception ex) {
            // Log and continue — cancellation should not fail if inventory release fails
            log.warn("Failed to release reserved stock for productId={}, quantity={}: {}", productId, quantity, ex.getMessage());
        }
    }

    // Helper method to build Order entity from Cart and CheckoutRequest
    private Order buildOrderFromCart(Cart cart, CheckoutRequest request
    ) {

        // In a real application, you would also validate the cart contents, check stock availability, and calculate totals here.
        Order order = Order.builder()
                .orderNumber(UUID.randomUUID().toString())
                .userId(cart.getUserId())
                .billingName(request.getBillingName().trim())
                .billingPhone(request.getBillingPhone().trim())
                .extraInformation(request.getExtraInformation() != null ? request.getExtraInformation().trim() : null)
                .shippingAddress(request.getShippingAddress().trim())
                .paymentMethod(request.getPaymentMethod())
                .orderStatus(OrderStatus.PENDING)
                .build();

        // Convert CartItems to OrderItems
        List<OrderItem> orderItems = cart
                .getCartItems()
                .stream()
                .map(cartItem ->
                        OrderItem.builder()
                                .order(order)
                                .productId(cartItem.getProductId())
                                .productTitle(cartItem.getProductTitle())
                                .productImage(cartItem.getProductImage())
                                .unitPrice(cartItem.getUnitPrice())
                                .discountPercent(cartItem.getDiscountPercent())
                                .quantity(cartItem.getQuantity())
                                .lineTotal(cartItem.getLineTotal())
                                .build()
                ).toList();

        // Set the order items and calculate the total amount
        order.setOrderItems(orderItems);
        BigDecimal totalAmount = orderItems
                .stream()
                .map(OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(totalAmount);
        return order;
    }

    // Helper method to convert Order entity to OrderResponse DTO
    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> orderItems = order
                .getOrderItems()
                .stream()
                .map(this::toItemResponse)
                .toList();

        // In a real application, you might want to include additional information in the response, such as product details or shipping info.
        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .billingName(order.getBillingName())
                .billingPhone(order.getBillingPhone())
                .paymentStatus(order.getPaymentStatus())
                .extraInformation(order.getExtraInformation())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId())
                .shippingAddress(order.getShippingAddress())
                .paymentMethod(order.getPaymentMethod())
                .orderStatus(order.getOrderStatus())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .cancelledAt(order.getCancelledAt())
                .orderItems(orderItems)
                .build();
    }

    // Helper method to convert OrderItem entity to OrderItemResponse DTO
    private OrderItemResponse toItemResponse(
            OrderItem item
    ) {

        return OrderItemResponse.builder()
                .orderItemId(item.getOrderItemId())
                .productId(item.getProductId())
                .productTitle(item.getProductTitle())
                .productImage(item.getProductImage())
                .unitPrice(item.getUnitPrice())
                .discountPercent(item.getDiscountPercent())
                .quantity(item.getQuantity())
                .lineTotal(item.getLineTotal())
                .build();
    }
}
