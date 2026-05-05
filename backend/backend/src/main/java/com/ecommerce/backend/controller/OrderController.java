package com.ecommerce.backend.controller;

import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.entity.Order.OrderStatus;
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

    @GetMapping
    @PreAuthorize("hasRole('admin')")
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('individual', 'corporate', 'admin')")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('individual', 'corporate', 'admin')")
    public List<Order> getOrdersByUser(@PathVariable Long userId) {
        return orderService.getOrdersByUser(userId);
    }

    @GetMapping("/store/{storeId}")
    @PreAuthorize("hasAnyRole('corporate', 'admin')")
    public List<Order> getOrdersByStore(@PathVariable Long storeId) {
        return orderService.getOrdersByStore(storeId);
    }

    // /api/orders/status/shipped → shipped siparişleri döner
    // Geçersiz status girilirse 400 Bad Request otomatik döner
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('corporate', 'admin')")
    public ResponseEntity<?> getOrdersByStatus(@PathVariable String status) {
        try {
            return ResponseEntity.ok(orderService.getOrdersByStatus(status));
        } catch (IllegalArgumentException e) {
            String valid = java.util.Arrays.stream(OrderStatus.values())
                    .map(Enum::name)
                    .collect(java.util.stream.Collectors.joining(", "));
            return ResponseEntity.badRequest()
                    .body("Geçersiz status: '" + status + "'. Geçerli değerler: " + valid);
        }
    }

    @GetMapping("/revenue/{storeId}")
    @PreAuthorize("hasAnyRole('corporate', 'admin')")
    public Double getTotalRevenue(@PathVariable Long storeId) {
        return orderService.getTotalRevenue(storeId);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('individual', 'admin')")
    public Order createOrder(@RequestBody Order order) {
        return orderService.createOrder(order);
    }

    // ?status=shipped şeklinde çağrılır
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('corporate', 'admin')")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        try {
            return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
        } catch (IllegalArgumentException e) {
            String valid = java.util.Arrays.stream(OrderStatus.values())
                    .map(Enum::name)
                    .collect(java.util.stream.Collectors.joining(", "));
            return ResponseEntity.badRequest()
                    .body("Geçersiz status: '" + status + "'. Geçerli değerler: " + valid);
        }
    }

    @GetMapping("/{orderId}/items")
    @PreAuthorize("hasAnyRole('individual', 'corporate', 'admin')")
    public List<OrderItem> getOrderItems(@PathVariable Long orderId) {
        return orderService.getOrderItems(orderId);
    }

    @GetMapping("/top-selling")
    @PreAuthorize("hasAnyRole('corporate', 'admin')")
    public List<Object[]> getTopSellingProducts() {
        return orderService.getTopSellingProducts();
    }

    @PutMapping("/{id}/refund")
    @PreAuthorize("hasAnyRole('individual', 'admin')")
    public Order refundOrder(@PathVariable Long id) {
        return orderService.refundOrder(id);
    }
}
