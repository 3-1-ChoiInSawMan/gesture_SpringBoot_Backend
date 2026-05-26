package chainsawman.gesture.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${brevo.template.verification-id}")
    private int templateId;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendVerificationCode(String toEmail, String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("api-key", apiKey);
        headers.set("accept", "application/json");
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "to", List.of(Map.of("email", toEmail)),
                "templateId", templateId,
                "params", Map.of("code", code)
        );

        restTemplate.postForEntity(BREVO_API_URL, new HttpEntity<>(body, headers), String.class);
    }
}
