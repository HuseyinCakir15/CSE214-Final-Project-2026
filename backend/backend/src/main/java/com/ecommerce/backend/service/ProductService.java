package com.ecommerce.backend.service;

import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    // Tüm ürünleri getir
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // ID'ye göre ürün getir
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    // İsme göre ürün ara
    public List<Product> searchByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    // Kategoriye göre ürün listele
    public List<Product> getProductsByCategory(String categoryName) {
        return productRepository.findByCategoryNameContainingIgnoreCase(categoryName);
    }

    // Mağazaya göre ürün listele
    public List<Product> getProductsByStore(String storeName) {
        return productRepository.findByStoreNameContainingIgnoreCase(storeName);
    }

    // En yüksek puanlı ürünler
    public List<Product> getTopRatedProducts() {
        return productRepository.findTop10ByOrderByRatingDesc();
    }

    // Yeni ürün ekle
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    // Ürün güncelle
    public Product updateProduct(Long id, Product updatedProduct) {
        updatedProduct.setId(id);
        return productRepository.save(updatedProduct);
    }

    // Ürün sil
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}