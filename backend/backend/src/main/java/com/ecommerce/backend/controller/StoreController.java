package com.ecommerce.backend.controller;

import com.ecommerce.backend.entity.Store;
import com.ecommerce.backend.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class StoreController {

    private final StoreService storeService;

    // Tüm mağazaları getir (admin)
    @GetMapping
    @PreAuthorize("hasRole('admin')")
    public List<Store> getAllStores() {
        return storeService.getAllStores();
    }

    // ID'ye göre mağaza getir
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('corporate', 'admin')")
    public ResponseEntity<Store> getStoreById(@PathVariable Long id) {
        return storeService.getStoreById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Kullanıcının mağazasını getir
    @GetMapping("/owner/{ownerId}")
    @PreAuthorize("hasAnyRole('corporate', 'admin')")
    public List<Store> getStoresByOwner(@PathVariable Long ownerId) {
        return storeService.getStoresByOwner(ownerId);
    }

    // Açık mağazaları getir (herkes)
    @GetMapping("/open")
    public List<Store> getOpenStores() {
        return storeService.getOpenStores();
    }

    // Yeni mağaza oluştur (corporate)
    @PostMapping
    @PreAuthorize("hasAnyRole('corporate', 'admin')")
    public ResponseEntity<?> createStore(@RequestBody Store store) {
        try {
            Store createdStore = storeService.createStore(store);
            return ResponseEntity.ok(createdStore);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Mağaza aç (sadece admin)
    @PutMapping("/{id}/open")
    @PreAuthorize("hasRole('admin')")
    public Store openStore(@PathVariable Long id) {
        return storeService.openStore(id);
    }

    // Mağaza kapat (sadece admin)
    @PutMapping("/{id}/close")
    @PreAuthorize("hasRole('admin')")
    public Store closeStore(@PathVariable Long id) {
        return storeService.closeStore(id);
    }

    // Mağaza sil (sadece admin)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Void> deleteStore(@PathVariable Long id) {
        storeService.deleteStore(id);
        return ResponseEntity.ok().build();
    }
}