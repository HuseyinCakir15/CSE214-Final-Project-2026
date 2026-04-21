package com.ecommerce.backend.controller;

import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.entity.OrderItem;
import com.ecommerce.backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class OrderController {

    private final OrderService orderService;

    // Tüm siparişleri getir (sadece admin)
    @GetMapping
    @PreAuthorize("hasRole('admin')")
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    // ID'ye göre sipariş getir
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('individual', 'corporate', 'admin')")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Kullanıcının siparişleri
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('individual', 'corporate', 'admin')")
    public List<Order> getOrdersByUser(@PathVariable Long userId) {
        return orderService.getOrdersByUser(userId);
    }

    // Mağazanın siparişleri (corporate ve admin)
    @GetMapping("/store/{storeId}")
    @PreAuthorize("hasAnyRole('corporate', 'admin')")
    public List<Order> getOrdersByStore(@PathVariable Long storeId) {
        return orderService.getOrdersByStore(storeId);
    }

    // Duruma göre siparişler
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('corporate', 'admin')")
    public List<Order> getOrdersByStatus(@PathVariable String status) {
        return orderService.getOrdersByStatus(status);
    }

    // Mağazanın toplam geliri
    @GetMapping("/revenue/{storeId}")
    @PreAuthorize("hasAnyRole('corporate', 'admin')")
    public Double getTotalRevenue(@PathVariable Long storeId) {
        return orderService.getTotalRevenue(storeId);
    }

    // Yeni sipariş oluştur (individual)
    @PostMapping
    @PreAuthorize("hasAnyRole('individual', 'admin')")
    public Order createOrder(@RequestBody Order order) {
        return orderService.createOrder(order);
    }

    // Sipariş durumunu güncelle (corporate ve admin)
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('corporate', 'admin')")
    public Order updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return orderService.updateOrderStatus(id, status);
    }

    // Sipariş kalemlerini getir
    @GetMapping("/{orderId}/items")
    @PreAuthorize("hasAnyRole('individual', 'corporate', 'admin')")
    public List<OrderItem> getOrderItems(@PathVariable Long orderId) {
        return orderService.getOrderItems(orderId);
    }

    // En çok satan ürünler (corporate ve admin)
    @GetMapping("/top-selling")
    @PreAuthorize("hasAnyRole('corporate', 'admin')")
    public List<Object[]> getTopSellingProducts() {
        return orderService.getTopSellingProducts();
    }

    // İade işlemi (individual)
    @PutMapping("/{id}/refund")
    @PreAuthorize("hasAnyRole('individual', 'admin')")
    public Order refundOrder(@PathVariable Long id) {
        return orderService.updateOrderStatus(id, "cancelled");
    }
}