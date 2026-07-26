package vn.pulsetech.auth.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import vn.pulsetech.auth.domain.AppUser;

import java.util.Optional;

public interface AppUserRepository extends MongoRepository<AppUser, String> {
    Optional<AppUser> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
}