package com.ecommerce.backend.service;

import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import com.ecommerce.backend.entity.Store;
import com.ecommerce.backend.service.StoreService;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StoreService storeService;

    // Tüm kullanıcıları getir (sadece admin)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // ID'ye göre kullanıcı getir
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // Email ile kullanıcı bul (login için)
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Role göre kullanıcıları listele
    public List<User> getUsersByRole(User.RoleType roleType) {
        return userRepository.findByRoleType(roleType);
    }

    // Yeni kullanıcı kaydet
    public User createUser(User user) {
        // Email kullanımda mı kontrol et
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Bu email zaten kullanımda!");
        }
        
        // Şifreyi hashle
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        
        // Kullanıcıyı kaydet
        User savedUser = userRepository.save(user);

        // EĞER KULLANICI CORPORATE İSE OTOMATİK MAĞAZA OLUŞTUR
        if (savedUser.getRoleType() == User.RoleType.corporate) {
            Store newStore = new Store();
            newStore.setName(savedUser.getEmail() + " Mağazası"); // Varsayılan isim
            newStore.setOwner(savedUser); // İlişkiyi kur
            newStore.setStatus(Store.Status.open); // Direkt açık başlat
            newStore.setCreatedAt(java.time.LocalDateTime.now());
            
            // storeRepository'yi buraya inject etmen veya storeService'i çağırman gerekir
            storeService.createStore(newStore); 
        }

        return savedUser;
    }

    // Kullanıcı sil (sadece admin)
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    // Kullanıcı var mı kontrol et
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
} 