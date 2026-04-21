package com.ecommerce.backend.service;

import com.ecommerce.backend.entity.Review;
import com.ecommerce.backend.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    // Tüm yorumları getir
    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    // ID'ye göre yorum getir
    public Optional<Review> getReviewById(Long id) {
        return reviewRepository.findById(id);
    }

    // Ürüne ait yorumları getir
    public List<Review> getReviewsByProduct(Long productId) {
        return reviewRepository.findByProductId(productId);
    }

    // Kullanıcının yorumlarını getir
    public List<Review> getReviewsByUser(Long userId) {
        return reviewRepository.findByUserId(userId);
    }

    // En çok yorum alan ürünler (hocanın soracağı soru!)
    public List<Object[]> getMostReviewedProducts() {
        return reviewRepository.findMostReviewedProducts();
    }

    // Ürünün ortalama puanı
    public Double getAverageRating(Long productId) {
        return reviewRepository.getAverageRatingByProductId(productId);
    }

    // Duyguya göre yorumlar
    public List<Review> getReviewsBySentiment(String sentiment) {
        return reviewRepository.findBySentiment(sentiment);
    }

    // Yeni yorum ekle
    public Review createReview(Review review) {
        return reviewRepository.save(review);
    }

    // Yorum sil
    public void deleteReview(Long id) {
        reviewRepository.deleteById(id);
    }
}