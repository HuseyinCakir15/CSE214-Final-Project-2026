package com.ecommerce.backend.controller;

import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

    private final UserService userService;

    // Tüm kullanıcıları getir (sadece admin)
    @GetMapping
    @PreAuthorize("hasRole('admin')")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    // ID'ye göre kullanıcı getir
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('individual', 'corporate', 'admin')")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Role göre kullanıcıları listele (sadece admin)
    @GetMapping("/role/{roleType}")
    @PreAuthorize("hasRole('admin')")
    public List<User> getUsersByRole(@PathVariable User.RoleType roleType) {
        return userService.getUsersByRole(roleType);
    }

    // Yeni kullanıcı kaydet (herkese açık, register için)
    @PostMapping("/register")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            User createdUser = userService.createUser(user);
            return ResponseEntity.ok(createdUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Kullanıcı sil (sadece admin)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }
}