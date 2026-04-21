package com.ecommerce.backend.service;

import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
        return userRepository.save(user);
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