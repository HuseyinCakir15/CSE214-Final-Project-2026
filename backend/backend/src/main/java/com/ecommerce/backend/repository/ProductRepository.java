package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // İsme göre ürün ara
    List<Product> findByNameContainingIgnoreCase(String name);

    // Kategori ismine göre ürün listele
    List<Product> findByCategoryNameContainingIgnoreCase(String categoryName);

    // Mağaza ismine göre ürün listele
    List<Product> findByStoreNameContainingIgnoreCase(String storeName);

    // En yüksek puanlı ürünler
    List<Product> findTop10ByOrderByRatingDesc();

    // Fiyat aralığına göre ürün ara
    List<Product> findByUnitPriceBetween(Double minPrice, Double maxPrice);
}