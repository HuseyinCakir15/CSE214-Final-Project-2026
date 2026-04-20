package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {
    List<Store> findByNameContainingIgnoreCase(String name);

    List<Store> findByStatus(Store.Status status);

    List<Store> findByOwnerId(Long ownerId);

    boolean existsByOwnerId(Long ownerId);
}