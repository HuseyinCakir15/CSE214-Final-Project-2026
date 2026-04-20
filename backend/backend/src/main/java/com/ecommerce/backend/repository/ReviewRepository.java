package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProductId(Long productId);

    List<Review> findByUserId(Long userId);

    List<Review> findByStarRating(Double starRating);

    List<Review> findBySentiment(String sentiment);

    @Query("SELECT r.product.id, r.product.name, COUNT(r) as reviewCount " +
           "FROM Review r GROUP BY r.product.id, r.product.name " +
           "ORDER BY reviewCount DESC")
    List<Object[]> findMostReviewedProducts();

    @Query("SELECT AVG(r.starRating) FROM Review r WHERE r.product.id = :productId")
    Double getAverageRatingByProductId(Long productId);

    @Query("SELECT AVG(r.starRating) FROM Review r WHERE r.product.store.id = :storeId")
    Double getAverageRatingByStoreId(Long storeId);
}