

package com.ecommerce.backend.service;

import com.ecommerce.backend.entity.Store;
import com.ecommerce.backend.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;

    // Tüm mağazaları getir (admin)
    public List<Store> getAllStores() {
        return storeRepository.findAll();
    }

    // ID'ye göre mağaza getir
    public Optional<Store> getStoreById(Long id) {
        return storeRepository.findById(id);
    }

    // Kullanıcının mağazasını getir
    public List<Store> getStoresByOwner(Long ownerId) {
        return storeRepository.findByOwnerId(ownerId);
    }

    // Açık mağazaları getir
    public List<Store> getOpenStores() {
        return storeRepository.findByStatus(Store.Status.open);
    }

    // Mağaza oluştur
    public Store createStore(Store store) {
        // Kullanıcının zaten mağazası var mı?
        if (storeRepository.existsByOwnerId(store.getOwner().getId())) {
            throw new RuntimeException("Bu kullanıcının zaten bir mağazası var!");
        }
        store.setStatus(Store.Status.open);
        return storeRepository.save(store);
    }

    // Mağaza aç (admin)
    public Store openStore(Long id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mağaza bulunamadı!"));
        store.setStatus(Store.Status.open);
        return storeRepository.save(store);
    }

    // Mağaza kapat (admin)
    public Store closeStore(Long id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mağaza bulunamadı!"));
        store.setStatus(Store.Status.closed);
        return storeRepository.save(store);
    }

    // Mağaza sil (admin)
    public void deleteStore(Long id) {
        storeRepository.deleteById(id);
    }
}