package com.ecommerce.backend.service;

import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.entity.Order.OrderStatus;
import com.ecommerce.backend.entity.OrderItem;
import com.ecommerce.backend.entity.Shipment;
import com.ecommerce.backend.entity.Shipment.ShipmentMode;
import com.ecommerce.backend.entity.Shipment.ShipmentStatus;
import com.ecommerce.backend.repository.OrderRepository;
import com.ecommerce.backend.repository.OrderItemRepository;
import com.ecommerce.backend.repository.ShipmentRepository;
import com.ecommerce.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ShipmentRepository shipmentRepository;
    private final ProductRepository productRepository;

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public List<Order> getOrdersByUser(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    public List<Order> getOrdersByStore(Long storeId) {
        return orderRepository.findByStoreId(storeId);
    }

    // String → Enum: controller'dan gelen String değeri Enum'a parse ediliyor
    public List<Order> getOrdersByStatus(String statusStr) {
        OrderStatus status = OrderStatus.valueOf(statusStr.toLowerCase());
        return orderRepository.findByStatus(status);
    }

    public Double getTotalRevenue(Long storeId) {
        return orderRepository.getTotalRevenueByStoreId(storeId);
    }

    @Transactional
    public Order createOrder(Order order) {
        order.setStatus(OrderStatus.pending);
        Order savedOrder = orderRepository.save(order);

        Shipment shipment = new Shipment();
        shipment.setOrder(savedOrder);
        shipment.setStatus(ShipmentStatus.pending);   // String yerine Enum
        shipment.setMode(ShipmentMode.Road);           // String yerine Enum
        shipment.setWarehouse("Warehouse-A");
        shipmentRepository.save(shipment);

        return savedOrder;
    }

    // String → Enum: geçersiz status gelirse IllegalArgumentException fırlar
    @Transactional
    public Order updateOrderStatus(Long id, String statusStr) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sipariş bulunamadı!"));

        OrderStatus status = OrderStatus.valueOf(statusStr.toLowerCase());
        order.setStatus(status);
        return orderRepository.save(order);
    }

    public List<OrderItem> getOrderItems(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    public List<Object[]> getTopSellingProducts() {
        return orderItemRepository.findTopSellingProducts();
    }

    @Transactional
    public Order refundOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sipariş bulunamadı!"));

        order.setStatus(OrderStatus.refunded);   // String yerine Enum
        orderRepository.save(order);

        Shipment shipment = shipmentRepository.findByOrderId(id);
        if (shipment != null) {
            shipment.setStatus(ShipmentStatus.returned);  // String yerine Enum
            shipmentRepository.save(shipment);
        }

        return order;
    }
}
