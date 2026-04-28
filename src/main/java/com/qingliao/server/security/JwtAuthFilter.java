package com.qingliao.server.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    private final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        String method = request.getMethod();
        log.debug("JwtAuthFilter: {} {} - checking...", method, path);

        if (path.startsWith("/api/auth/") || path.startsWith("/uploads/") || path.startsWith("/ws")) {
            log.debug("JwtAuthFilter: {} {} - skipped (public path)", method, path);
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractToken(request);
        log.debug("JwtAuthFilter: {} {} - token present: {}", method, path, token != null);
        if (token != null && jwtUtil.validateToken(token)) {
            Long userId = jwtUtil.getUserIdFromToken(token);
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(auth);
            log.debug("JwtAuthFilter: {} {} - auth set for userId={}", method, path, userId);
        } else if (token != null) {
            log.warn("JwtAuthFilter: {} {} - token validation FAILED", method, path);
        } else {
            log.warn("JwtAuthFilter: {} {} - no token in request", method, path);
        }
        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return request.getParameter("token");
    }
}
