package com.ecommerce.backend.controller;

import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ProductController {

    private final ProductService productService;

    // Tüm ürünleri getir (herkes erişebilir)
    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    // ID'ye göre ürün getir
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // İsme göre ürün ara
    @GetMapping("/search")
    public List<Product> searchProducts(@RequestParam String name) {
        return productService.searchByName(name);
    }

    // Kategoriye göre ürün listele
    @GetMapping("/category")
    public List<Product> getProductsByCategory(@RequestParam String categoryName) {
        return productService.getProductsByCategory(categoryName);
    }

    // En yüksek puanlı ürünler
    @GetMapping("/top-rated")
    public List<Product> getTopRatedProducts() {
        return productService.getTopRatedProducts();
    }

    // Yeni ürün ekle (sadece corporate ve admin)
    @PostMapping
    @PreAuthorize("hasAnyRole('corporate', 'admin')")
    public Product createProduct(@RequestBody Product product) {
        return productService.createProduct(product);
    }

    // Ürün güncelle (sadece corporate ve admin)
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('corporate', 'admin')")
    public Product updateProduct(@PathVariable Long id, @RequestBody Product product) {
        return productService.updateProduct(id, product);
    }

    // Ürün sil (sadece admin)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok().build();
    }
}