package vn.pulsetech.auth.security;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class JwtService {
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();
    private final ObjectMapper objectMapper;
    private final byte[] secret;
    private final long expirationHours;

    public JwtService(ObjectMapper objectMapper,
                      @Value("${app.jwt-secret}") String secret,
                      @Value("${app.jwt-expiration-hours:12}") long expirationHours) {
        if (secret == null || secret.length() < 32) throw new IllegalStateException("JWT_SECRET phải có tối thiểu 32 ký tự");
        this.objectMapper = objectMapper;
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationHours = expirationHours;
    }

    public IssuedToken issue(String userId, String email, Set<String> roles) {
        Instant expiresAt = Instant.now().plus(expirationHours, ChronoUnit.HOURS);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", userId);
        payload.put("email", email);
        payload.put("roles", roles);
        payload.put("iat", Instant.now().getEpochSecond());
        payload.put("exp", expiresAt.getEpochSecond());
        try {
            String header = ENCODER.encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
            String body = ENCODER.encodeToString(objectMapper.writeValueAsBytes(payload));
            String content = header + "." + body;
            return new IssuedToken(content + "." + sign(content), expiresAt);
        } catch (Exception exception) {
            throw new IllegalStateException("Không thể tạo access token", exception);
        }
    }

    public Claims verify(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) throw new IllegalArgumentException("Token không hợp lệ");
            String content = parts[0] + "." + parts[1];
            if (!java.security.MessageDigest.isEqual(sign(content).getBytes(StandardCharsets.UTF_8),
                    parts[2].getBytes(StandardCharsets.UTF_8))) throw new IllegalArgumentException("Chữ ký token không hợp lệ");
            Map<String, Object> payload = objectMapper.readValue(DECODER.decode(parts[1]), new TypeReference<>() {});
            long exp = ((Number) payload.get("exp")).longValue();
            if (Instant.now().getEpochSecond() >= exp) throw new IllegalArgumentException("Token đã hết hạn");
            @SuppressWarnings("unchecked") List<String> roles = (List<String>) payload.getOrDefault("roles", List.of());
            return new Claims(String.valueOf(payload.get("sub")), String.valueOf(payload.get("email")), Set.copyOf(roles));
        } catch (Exception exception) {
            throw new IllegalArgumentException("Access token không hợp lệ", exception);
        }
    }

    private String sign(String content) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret, "HmacSHA256"));
        return ENCODER.encodeToString(mac.doFinal(content.getBytes(StandardCharsets.UTF_8)));
    }

    public record IssuedToken(String value, Instant expiresAt) {}
    public record Claims(String userId, String email, Set<String> roles) {}
}
