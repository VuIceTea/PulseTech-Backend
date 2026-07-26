package vn.pulsetech.auth.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import vn.pulsetech.auth.domain.EmailVerificationToken;

import java.util.Optional;

public interface EmailVerificationTokenRepository extends MongoRepository<EmailVerificationToken, String> {
    Optional<EmailVerificationToken> findByToken(String token);
    void deleteByUserId(String userId);
}