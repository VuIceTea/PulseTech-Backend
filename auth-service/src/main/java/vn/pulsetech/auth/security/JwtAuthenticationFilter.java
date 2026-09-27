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
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization != null && authorization.startsWith("Bearer ")) {
            try {
                JwtService.Claims claims = jwtService.verify(authorization.substring(7).trim());
                java.util.List<org.springframework.security.core.GrantedAuthority> authorities = claims.roles().stream()
                        .map(role -> new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                        .collect(java.util.stream.Collectors.toList());
                org.springframework.security.authentication.UsernamePasswordAuthenticationToken authentication =
                        new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(claims.email(), null, authorities);
                org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authentication);
                request.setAttribute("adminEmail", claims.email());
                request.setAttribute("adminUserId", claims.userId());
            } catch (Exception ignored) {
                org.springframework.security.core.context.SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), Map.of("status", status, "message", message));
    }
}
