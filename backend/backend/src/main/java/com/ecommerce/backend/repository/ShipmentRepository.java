package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    Shipment findByOrderId(Long orderId);

    List<Shipment> findByStatus(String status);

    List<Shipment> findByCity(String city);

    List<Shipment> findByMode(String mode);

    @Query("SELECT COUNT(s) FROM Shipment s WHERE s.status = 'pending'")
    Long getPendingShipmentCount();
}