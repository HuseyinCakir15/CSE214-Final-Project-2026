package com.ecommerce.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sku;
    private String name;

    @Column(name = "unit_price")
    private Double unitPrice;

    @Column(name = "discounted_price")
    private Double discountedPrice;

    private Integer stock;
    private Double rating;

    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
}