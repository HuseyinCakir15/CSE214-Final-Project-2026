package com.ecommerce.backend.service;

import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.entity.OrderItem;
import com.ecommerce.backend.entity.Shipment;
import com.ecommerce.backend.repository.OrderRepository;
import com.ecommerce.backend.repository.OrderItemRepository;
import com.ecommerce.backend.repository.ShipmentRepository;
import com.ecommerce.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ShipmentRepository shipmentRepository;
    private final ProductRepository productRepository;

    // Tüm siparişleri getir (admin)
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // ID'ye göre sipariş getir
    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    // Kullanıcının siparişlerini getir
    public List<Order> getOrdersByUser(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    // Mağazanın siparişlerini getir
    public List<Order> getOrdersByStore(Long storeId) {
        return orderRepository.findByStoreId(storeId);
    }

    // Duruma göre siparişleri getir
    public List<Order> getOrdersByStatus(String status) {
        return orderRepository.findByStatus(status);
    }

    // Mağazanın toplam geliri
    public Double getTotalRevenue(Long storeId) {
        return orderRepository.getTotalRevenueByStoreId(storeId);
    }

    // Sipariş oluştur
    public Order createOrder(Order order) {
        // Siparişi kaydet
        Order savedOrder = orderRepository.save(order);

        // Kargo kaydı otomatik oluştur
        Shipment shipment = new Shipment();
        shipment.setOrder(savedOrder);
        shipment.setStatus("pending");
        shipment.setMode("Road");
        shipment.setWarehouse("Warehouse-A");
        shipmentRepository.save(shipment);

        return savedOrder;
    }

    // Sipariş durumunu güncelle
    public Order updateOrderStatus(Long id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sipariş bulunamadı!"));
        order.setStatus(status);
        return orderRepository.save(order);
    }

    // Sipariş kalemlerini getir
    public List<OrderItem> getOrderItems(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    // En çok satan ürünler
    public List<Object[]> getTopSellingProducts() {
        return orderItemRepository.findTopSellingProducts();
    }

    // İade işlemi
public Order refundOrder(Long id) {
    Order order = orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Sipariş bulunamadı!"));
    
    // Sipariş durumunu güncelle
    order.setStatus("cancelled");
    orderRepository.save(order);

    // Kargo durumunu güncelle
    Shipment shipment = shipmentRepository.findByOrderId(id);
    if (shipment != null) {
        shipment.setStatus("returned");
        shipmentRepository.save(shipment);
    }

    return order;
}
}