package vn.pulsetech.auth.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import vn.pulsetech.auth.domain.AppUser;
import vn.pulsetech.auth.domain.EmailVerificationToken;
import vn.pulsetech.auth.dto.AuthDtos.*;
import vn.pulsetech.auth.repository.AppUserRepository;
import vn.pulsetech.auth.repository.EmailVerificationTokenRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.UUID;

@Service
public class AuthService {
    private final AppUserRepository users;
    private final EmailVerificationTokenRepository tokens;
    private final PasswordEncoder passwordEncoder;
    private final VerificationEmailService emailService;

    public AuthService(AppUserRepository users, EmailVerificationTokenRepository tokens,
                       PasswordEncoder passwordEncoder, VerificationEmailService emailService) {
        this.users = users;
        this.tokens = tokens;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public RegisterResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (users.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email đã được sử dụng");
        }

        AppUser user = users.save(new AppUser(request.name().trim(), email,
                passwordEncoder.encode(request.password())));
        EmailVerificationToken verification = tokens.save(new EmailVerificationToken(
                UUID.randomUUID().toString(), user.getId(), Instant.now().plus(24, ChronoUnit.HOURS)));
        try {
            emailService.send(email, verification.getToken());
        } catch (RuntimeException exception) {
            tokens.delete(verification);
            users.delete(user);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Không thể gửi email xác thực. Vui lòng thử lại", exception);
        }
        return new RegisterResponse(email,
                "Đăng ký thành công. Vui lòng kiểm tra email để xác thực tài khoản");
    }

    public UserResponse login(LoginRequest request) {
        AppUser user = users.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(this::unauthorized);
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw unauthorized();
        }
        if (!user.isVerified()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Vui lòng xác thực email trước khi đăng nhập");
        }
        return UserResponse.from(user);
    }

    public VerifyResponse verify(String rawToken) {
        String token = rawToken == null ? "" : rawToken.trim();
        EmailVerificationToken verification = tokens.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Liên kết xác thực không hợp lệ hoặc đã được sử dụng"));
        if (verification.getExpiresAt().isBefore(Instant.now())) {
            tokens.delete(verification);
            throw new ResponseStatusException(HttpStatus.GONE, "Liên kết xác thực đã hết hạn");
        }
        AppUser user = users.findById(verification.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy tài khoản cần xác thực"));
        user.markVerified();
        users.save(user);
        tokens.deleteByUserId(user.getId());
        return new VerifyResponse("Xác thực email thành công");
    }

    public java.util.List<UserResponse> getAllUsers() {
        return users.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    public void deleteUser(String id) {
        users.deleteById(id);
    }

    private ResponseStatusException unauthorized() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email hoặc mật khẩu không đúng");
    }
}