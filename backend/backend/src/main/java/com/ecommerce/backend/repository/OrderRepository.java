package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.entity.Order.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserId(Long userId);

    List<Order> findByStoreId(Long storeId);

    // String → Enum tipine güncellendi
    List<Order> findByStatus(OrderStatus status);

    List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);

    @Query("SELECT SUM(o.grandTotal) FROM Order o WHERE o.store.id = :storeId")
    Double getTotalRevenueByStoreId(Long storeId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.store.id = :storeId")
    Long getOrderCountByStoreId(Long storeId);
}
