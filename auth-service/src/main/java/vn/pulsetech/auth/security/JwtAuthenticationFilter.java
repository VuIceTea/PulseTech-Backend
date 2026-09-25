package vn.pulsetech.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;
    public JwtAuthenticationFilter(JwtService jwtService, ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!request.getRequestURI().startsWith("/api/auth/admin/")) {
            filterChain.doFilter(request, response);
            return;
        }
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            writeError(response, 401, "Bạn cần đăng nhập bằng tài khoản quản trị");
            return;
        }
        try {
            JwtService.Claims claims = jwtService.verify(authorization.substring(7).trim());
            if (!claims.roles().contains("ADMIN")) {
                writeError(response, 403, "Tài khoản không có quyền ADMIN");
                return;
            }
            request.setAttribute("adminEmail", claims.email());
            request.setAttribute("adminUserId", claims.userId());
            filterChain.doFilter(request, response);
        } catch (IllegalArgumentException exception) {
            writeError(response, 401, "Access token không hợp lệ hoặc đã hết hạn");
        }
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), Map.of("status", status, "message", message));
    }
}
