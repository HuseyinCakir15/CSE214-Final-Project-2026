package com.ecommerce.backend.service;

import com.ecommerce.backend.entity.Review;
import com.ecommerce.backend.entity.Review.Sentiment;
import com.ecommerce.backend.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    public Optional<Review> getReviewById(Long id) {
        return reviewRepository.findById(id);
    }

    public List<Review> getReviewsByProduct(Long productId) {
        return reviewRepository.findByProductId(productId);
    }

    public List<Review> getReviewsByUser(Long userId) {
        return reviewRepository.findByUserId(userId);
    }

    public List<Object[]> getMostReviewedProducts() {
        return reviewRepository.findMostReviewedProducts();
    }

    public Double getAverageRating(Long productId) {
        return reviewRepository.getAverageRatingByProductId(productId);
    }

    // String → Enum: geçersiz sentiment gelirse IllegalArgumentException fırlar
    public List<Review> getReviewsBySentiment(String sentimentStr) {
        Sentiment sentiment = Sentiment.valueOf(sentimentStr.toLowerCase());
        return reviewRepository.findBySentiment(sentiment);
    }

    public Review createReview(Review review) {
        return reviewRepository.save(review);
    }

    public void deleteReview(Long id) {
        reviewRepository.deleteById(id);
    }
}