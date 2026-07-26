package vn.pulsetech.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import vn.pulsetech.auth.domain.AppUser;

public final class AuthDtos {
    private AuthDtos() {}

    public record RegisterRequest(@NotBlank String name, @Email @NotBlank String email,
                                  @Size(min = 6) String password) {}
    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}
    public record UserResponse(String id, String name, String email) {
        public static UserResponse from(AppUser user) {
            return new UserResponse(user.getId(), user.getName(), user.getEmail());
        }
    }
    public record RegisterResponse(String email, String message) {}
    public record VerifyResponse(String message) {}
}