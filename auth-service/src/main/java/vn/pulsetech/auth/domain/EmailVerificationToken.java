package vn.pulsetech.auth.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "email_verification_tokens")
public class EmailVerificationToken {
    @Id
    private String id;
    @Indexed(unique = true)
    private String token;
    @Indexed
    private String userId;
    private Instant expiresAt;

    protected EmailVerificationToken() {}

    public EmailVerificationToken(String token, String userId, Instant expiresAt) {
        this.token = token;
        this.userId = userId;
        this.expiresAt = expiresAt;
    }

    public String getToken() { return token; }
    public String getUserId() { return userId; }
    public Instant getExpiresAt() { return expiresAt; }
}