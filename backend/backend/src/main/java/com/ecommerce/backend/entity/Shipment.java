package com.ecommerce.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "shipments")
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String warehouse;

    // String yerine Enum — taşıma modu
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentMode mode = ShipmentMode.Road;

    // String yerine Enum — kargo durumu
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentStatus status = ShipmentStatus.pending;

    private String city;
    private String state;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ─── İç Enum'lar ───────────────────────────────────────
    public enum ShipmentStatus {
        pending,
        in_transit,
        delivered,
        returned
    }

    public enum ShipmentMode {
        Road,
        Air,
        Ship
    }
}
