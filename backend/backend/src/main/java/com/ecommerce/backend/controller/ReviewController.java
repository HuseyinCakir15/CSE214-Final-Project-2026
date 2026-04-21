package com.ecommerce.backend.controller;

import com.ecommerce.backend.entity.Review;
import com.ecommerce.backend.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ReviewController {

    private final ReviewService reviewService;

    // Tüm yorumları getir (herkes)
    @GetMapping
    public List<Review> getAllReviews() {
        return reviewService.getAllReviews();
    }

    // ID'ye göre yorum getir
    @GetMapping("/{id}")
    public ResponseEntity<Review> getReviewById(@PathVariable Long id) {
        return reviewService.getReviewById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Ürüne ait yorumları getir (herkes)
    @GetMapping("/product/{productId}")
    public List<Review> getReviewsByProduct(@PathVariable Long productId) {
        return reviewService.getReviewsByProduct(productId);
    }

    // Kullanıcının yorumlarını getir
    @GetMapping("/user/{userId}")
    public List<Review> getReviewsByUser(@PathVariable Long userId) {
        return reviewService.getReviewsByUser(userId);
    }

    // En çok yorum alan ürünler (hocanın soracağı soru!)
    @GetMapping("/most-reviewed")
    public List<Object[]> getMostReviewedProducts() {
        return reviewService.getMostReviewedProducts();
    }

    // Ürünün ortalama puanı
    @GetMapping("/average-rating/{productId}")
    public Double getAverageRating(@PathVariable Long productId) {
        return reviewService.getAverageRating(productId);
    }

    // Duyguya göre yorumlar
    @GetMapping("/sentiment")
    public List<Review> getReviewsBySentiment(@RequestParam String sentiment) {
        return reviewService.getReviewsBySentiment(sentiment);
    }

    // Yeni yorum ekle (individual, corporate, admin)
    @PostMapping
    @PreAuthorize("hasAnyRole('individual', 'corporate', 'admin')")
    public Review createReview(@RequestBody Review review) {
        return reviewService.createReview(review);
    }

    // Yorum sil (sadece admin)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok().build();
    }
}