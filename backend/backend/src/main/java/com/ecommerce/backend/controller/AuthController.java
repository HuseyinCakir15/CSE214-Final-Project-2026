package com.ecommerce.backend.controller;

import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.security.JwtUtil;
import com.ecommerce.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    // UserRepository yerine UserService — katmanlı mimari korundu
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String email    = request.get("email");
        String password = request.get("password");

        Optional<User> userOpt = userService.getUserByEmail(email);

        // ✅ Email enumeration açığı kapatıldı:
        // "Kullanıcı bulunamadı" ve "Şifre yanlış" ayrı mesajlar
        // saldırganın hangi email'in kayıtlı olduğunu anlamasına izin veriyordu.
        // Her iki durumda da aynı mesaj döndürülüyor.
        if (userOpt.isEmpty() || !passwordEncoder.matches(password, userOpt.get().getPasswordHash())) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", "Email veya şifre hatalı!"));
        }

        User user = userOpt.get();
        String token = jwtUtil.generateToken(email, user.getRoleType().name());

        return ResponseEntity.ok(Map.of(
                "token", token,
                "role",  user.getRoleType().name(),
                "email", email,
                "id",    user.getId()
        ));
    }
}