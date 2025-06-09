package com.ssai.lumilovebackend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssai.lumilovebackend.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod; // Import HttpMethod
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // 添加条件：如果是 OPTIONS 请求且路径以 /api/ 开头，则不进行过滤
        if (HttpMethod.OPTIONS.matches(request.getMethod()) && path.startsWith("/api/")) {
            return true;
        }
        return path.equals("/api/auth/login") ||
                path.equals("/api/auth/register") ||
                path.equals("/api/auth/refresh");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {


        // 清除之前的认证信息
        SecurityContextHolder.clearContext();

        final String authHeader = request.getHeader("Authorization");
        logger.debug("Processing request to: {}", request.getRequestURI());

        // 注意：这里的逻辑只会在 shouldNotFilter 返回 false 时执行
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.debug("No Bearer token found in request");
            handleAuthenticationError(response, "No token provided");
            return;
        }

        try {
            final String jwt = authHeader.substring(7);
            logger.debug("Extracted JWT token: {}", jwt);

            // 先验证token
            if (!jwtUtil.validateToken(jwt)) {
                logger.warn("JWT token validation failed");
                handleAuthenticationError(response, "Token has expired or is invalid");
                return;
            }

            final String userEmail = jwtUtil.getEmailFromToken(jwt);
            logger.debug("Extracted user email from token: {}", userEmail);

            if (userEmail != null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
                logger.debug("Loaded user details for email: {}", userEmail);

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                logger.debug("Authentication set in SecurityContext");
                logger.debug("SecurityContextHolder after setting authentication: {}", SecurityContextHolder.getContext().getAuthentication());
            }
        } catch (Exception e) {
            logger.error("Error processing JWT token: {}", e.getMessage(), e);
            handleAuthenticationError(response, "Error processing token");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void handleAuthenticationError(HttpServletResponse response, String message) throws IOException {
        // 确保清除认证信息
        SecurityContextHolder.clearContext();

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, String> error = new HashMap<>();
        error.put("message", message);

        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}