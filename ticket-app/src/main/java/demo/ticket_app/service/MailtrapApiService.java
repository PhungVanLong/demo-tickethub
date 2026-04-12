package demo.ticket_app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class MailtrapApiService {
    @Value("${mailtrap.api.token}")
    private String apiToken;

    @Value("${mailtrap.from.email}")
    private String fromEmail;

    @Value("${mailtrap.from.name}")
    private String fromName;

    private static final String API_URL = "https://send.api.mailtrap.io/api/send";

    public void sendOtpEmail(String to, String otp) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("from", Map.of("email", fromEmail, "name", fromName));
        body.put("to", List.of(Map.of("email", to)));
        body.put("subject", "Your OTP Code");
        body.put("text", "Your OTP code is: " + otp);
        body.put("category", "OTP");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(API_URL, request, String.class);
        } catch (Exception e) {
            throw new RuntimeException("Gửi email qua Mailtrap API thất bại: " + e.getMessage(), e);
        }
    }
}
