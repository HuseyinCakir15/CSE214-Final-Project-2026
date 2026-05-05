package com.ecommerce.backend.service;

import com.ecommerce.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final UserRepository userRepository;

    @Value("${chatbot.url:http://localhost:8000}")
    private String chatbotUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> ask(String question, String role, String email) {
        try {
            // Email'den userId bul
            Long userId = userRepository.findByEmail(email)
                    .map(u -> u.getId())
                    .orElse(1L);

            // Python'a gönderilecek payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("question", question);
            payload.put("role", role);
            payload.put("user_id", userId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            // Python FastAPI'ye ilet
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    chatbotUrl + "/chat/ask",
                    entity,
                    Map.class
            );

            return response.getBody() != null ? response.getBody() : defaultError();

        } catch (Exception e) {
            System.err.println("Chatbot service error: " + e.getMessage());
            return defaultError();
        }
    }

    private Map<String, Object> defaultError() {
        Map<String, Object> error = new HashMap<>();
        error.put("answer", "AI asistan şu an yanıt veremiyor. Lütfen daha sonra tekrar deneyin.");
        error.put("sql", null);
        error.put("data", null);
        error.put("plotly_json", null);
        return error;
    }
}