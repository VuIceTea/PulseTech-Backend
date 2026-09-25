package vn.pulsetech.auth.service;

import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import vn.pulsetech.auth.security.JwtService;

@Service
public class AuthService {
    private final AppUserRepository users;
    private final EmailVerificationTokenRepository tokens;
    private final PasswordEncoder passwordEncoder;
    private final VerificationEmailService emailService;
    private final JwtService jwtService;
    private final Set<String> configuredAdmins;

    public AuthService(AppUserRepository users, EmailVerificationTokenRepository tokens,
                       PasswordEncoder passwordEncoder, VerificationEmailService emailService,
                       JwtService jwtService, @Value("${app.admin-emails:}") String adminEmails) {
        this.users = users;
        this.tokens = tokens;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.jwtService = jwtService;
        this.configuredAdmins = Arrays.stream(adminEmails.split(","))
                .map(value -> value.trim().toLowerCase(Locale.ROOT)).filter(value -> !value.isBlank())
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
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

    public LoginResponse login(LoginRequest request) {
        AppUser user = users.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(this::unauthorized);
        if (user.isLocked()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tài khoản đã bị khóa");
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw unauthorized();
        }
        if (!user.isVerified()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tài khoản chưa được xác thực");
        }
        if (configuredAdmins.contains(user.getEmail().toLowerCase(Locale.ROOT)) && !user.getRoles().contains("ADMIN")) {
            Set<String> roles = new LinkedHashSet<>(user.getRoles());
            roles.add("ADMIN");
            user.setRoles(roles);
            user = users.save(user);
        }
        JwtService.IssuedToken token = jwtService.issue(user.getId(), user.getEmail(), user.getRoles());
        return new LoginResponse(user.getId(), user.getName(), user.getEmail(), user.getRewardPoints(),
                user.isVerified(), user.isLocked(), user.getCreatedAt(), Set.copyOf(user.getRoles()),
                token.value(), "Bearer", token.expiresAt());
    }

    public void addRewardPoints(String email, int points) {
        AppUser user = users.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Người dùng không tồn tại"));
        user.addRewardPoints(points);
        users.save(user);
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

    public UserResponse updateUserLocked(String id, boolean locked) {
        AppUser user = users.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Người dùng không tồn tại"));
        user.setLocked(locked);
        return UserResponse.from(users.save(user));
    }

    public UserResponse getUser(String id) {
        return UserResponse.from(findUser(id));
    }

    public UserResponse updateUserLocked(String id, boolean locked, String adminEmail) {
        AppUser user = findUser(id);
        if (user.getEmail().equalsIgnoreCase(adminEmail) && locked) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không thể tự khóa tài khoản đang đăng nhập");
        }
        user.setLocked(locked);
        return UserResponse.from(users.save(user));
    }

    public UserResponse updateUserRoles(String id, Set<String> requestedRoles, String adminEmail) {
        AppUser user = findUser(id);
        Set<String> roles = normalizeRoles(requestedRoles);
        boolean removingAdmin = user.getRoles().contains("ADMIN") && !roles.contains("ADMIN");
        if (user.getEmail().equalsIgnoreCase(adminEmail) && removingAdmin) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không thể tự gỡ quyền ADMIN của tài khoản đang đăng nhập");
        }
        if (removingAdmin && countAdmins() <= 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hệ thống phải còn ít nhất một tài khoản ADMIN");
        }
        user.setRoles(roles);
        return UserResponse.from(users.save(user));
    }

    public void deleteUser(String id, String adminEmail) {
        AppUser user = findUser(id);
        if (user.getEmail().equalsIgnoreCase(adminEmail)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không thể tự xóa tài khoản đang đăng nhập");
        }
        if (user.getRoles().contains("ADMIN") && countAdmins() <= 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hệ thống phải còn ít nhất một tài khoản ADMIN");
        }
        users.delete(user);
    }

    private AppUser findUser(String id) {
        return users.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Người dùng không tồn tại"));
    }

    private Set<String> normalizeRoles(Set<String> requestedRoles) {
        if (requestedRoles == null || requestedRoles.isEmpty()) return Set.of("USER");
        Set<String> roles = requestedRoles.stream().map(String::trim).map(value -> value.toUpperCase(Locale.ROOT))
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        Set<String> allowed = Set.of("USER", "STAFF", "ADMIN");
        if (!allowed.containsAll(roles)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role chỉ được phép là USER, STAFF hoặc ADMIN");
        }
        roles.add("USER");
        return roles;
    }

    private long countAdmins() {
        return users.findAll().stream().filter(user -> user.getRoles().contains("ADMIN")).count();
    }

    private ResponseStatusException unauthorized() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email hoặc mật khẩu không đúng");
    }
}
