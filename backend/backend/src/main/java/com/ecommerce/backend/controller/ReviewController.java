package com.ecommerce.backend.controller;

import com.ecommerce.backend.entity.Review;
import com.ecommerce.backend.entity.Review.Sentiment;
import com.ecommerce.backend.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public List<Review> getAllReviews() {
        return reviewService.getAllReviews();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Review> getReviewById(@PathVariable Long id) {
        return reviewService.getReviewById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/product/{productId}")
    public List<Review> getReviewsByProduct(@PathVariable Long productId) {
        return reviewService.getReviewsByProduct(productId);
    }

    @GetMapping("/user/{userId}")
    public List<Review> getReviewsByUser(@PathVariable Long userId) {
        return reviewService.getReviewsByUser(userId);
    }

    @GetMapping("/most-reviewed")
    public List<Object[]> getMostReviewedProducts() {
        return reviewService.getMostReviewedProducts();
    }

    @GetMapping("/average-rating/{productId}")
    public Double getAverageRating(@PathVariable Long productId) {
        return reviewService.getAverageRating(productId);
    }

    // /api/reviews/sentiment?sentiment=positive
    // Geçersiz değer gelirse 400 + geçerli seçenekler döner
    @GetMapping("/sentiment")
    public ResponseEntity<?> getReviewsBySentiment(@RequestParam String sentiment) {
        try {
            return ResponseEntity.ok(reviewService.getReviewsBySentiment(sentiment));
        } catch (IllegalArgumentException e) {
            String valid = Arrays.stream(Sentiment.values())
                    .map(Enum::name)
                    .collect(Collectors.joining(", "));
            return ResponseEntity.badRequest()
                    .body("Geçersiz sentiment: '" + sentiment + "'. Geçerli değerler: " + valid);
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('individual', 'corporate', 'admin')")
    public Review createReview(@RequestBody Review review) {
        return reviewService.createReview(review);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok().build();
    }
}