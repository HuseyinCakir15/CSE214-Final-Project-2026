package com.ecommerce.backend.controller;

import com.ecommerce.backend.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/ask")
    @PreAuthorize("hasAnyRole('admin', 'corporate', 'individual')")
    public ResponseEntity<?> ask(
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        String question = request.get("question");
        if (question == null || question.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("answer", "Lütfen bir soru girin."));
        }

        // JWT'den rol ve email al — Angular'dan gelen role/userId'ye güvenmiyoruz
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", "").toLowerCase())
                .orElse("individual");

        String email = authentication.getName();

        // Python servisine ilet
        Map<String, Object> response = chatService.ask(question, role, email);
        return ResponseEntity.ok(response);
    }
}