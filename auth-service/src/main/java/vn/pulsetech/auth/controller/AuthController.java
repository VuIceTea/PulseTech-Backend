package vn.pulsetech.auth.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vn.pulsetech.auth.dto.AuthDtos.*;
import vn.pulsetech.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService service;
    private final String internalApiKey;
    public AuthController(AuthService service, @Value("${app.internal-api-key:}") String internalApiKey) {
        this.service = service;
        this.internalApiKey = internalApiKey;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        return service.register(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return service.login(request);
    }

    @GetMapping("/verify")
    public VerifyResponse verify(@RequestParam String token) {
        return service.verify(token);
    }

    @GetMapping("/admin/users")
    public java.util.List<UserResponse> getAllUsers() {
        return service.getAllUsers();
    }

    @GetMapping("/admin/users/{id}")
    public UserResponse getUser(@PathVariable String id) { return service.getUser(id); }

    @PatchMapping("/admin/users/{id}/locked")
    public UserResponse updateLocked(@PathVariable String id, @RequestBody LockRequest request, HttpServletRequest httpRequest) {
        return service.updateUserLocked(id, request.locked(), String.valueOf(httpRequest.getAttribute("adminEmail")));
    }

    @PatchMapping("/admin/users/{id}/roles")
    public UserResponse updateRoles(@PathVariable String id, @RequestBody RoleRequest request, HttpServletRequest httpRequest) {
        return service.updateUserRoles(id, request.roles(), String.valueOf(httpRequest.getAttribute("adminEmail")));
    }

    @PostMapping("/users/reward-points")
    @ResponseStatus(HttpStatus.OK)
    public void addRewardPoints(@RequestParam String email, @RequestParam int points,
                                @RequestHeader(value = "X-Internal-Api-Key", required = false) String apiKey) {
        if (internalApiKey.isBlank() || !java.security.MessageDigest.isEqual(
                internalApiKey.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                String.valueOf(apiKey).getBytes(java.nio.charset.StandardCharsets.UTF_8))) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN, "Internal API key không hợp lệ");
        }
        service.addRewardPoints(email, points);
    }

    @DeleteMapping("/admin/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable String id, HttpServletRequest httpRequest) {
        service.deleteUser(id, String.valueOf(httpRequest.getAttribute("adminEmail")));
    }
}
