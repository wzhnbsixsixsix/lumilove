package com.ssai.lumilovebackend.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    private SecretKey key;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    @PostConstruct
    public void init() {
        this.key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        logger.info("JWT initialized with expiration: {} seconds, refresh expiration: {} seconds", 
                   expiration, refreshExpiration);
    }

    public String generateToken(Long userId, String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);
        claims.put("type", "access");
        String token = createToken(claims, expiration);
        logger.debug("Generated access token for user {}: {}", email, token);
        return token;
    }

    public String generateRefreshToken(Long userId, String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);
        claims.put("type", "refresh");
        String token = createToken(claims, refreshExpiration);
        logger.debug("Generated refresh token for user {}: {}", email, token);
        return token;
    }

    private String createToken(Map<String, Object> claims, Long expirationTime) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime * 1000);
        
        logger.trace("Creating token with claims: {}, expiration: {}", claims, expiryDate);
        
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public Boolean validateToken(String token) {
        try {
            logger.trace("Validating token: {}", token);
            Claims claims = getAllClaimsFromToken(token);
            boolean isValid = !isTokenExpired(token) && isAccessToken(token);
            logger.debug("Token validation result: {}, claims: {}", isValid, claims);
            return isValid;
        } catch (ExpiredJwtException e) {
            logger.warn("Token expired: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Error validating token: {}", e.getMessage());
            return false;
        }
    }

    public Boolean validateRefreshToken(String token) {
        try {
            logger.trace("Validating refresh token: {}", token);
            Claims claims = getAllClaimsFromToken(token);
            boolean isValid = !isTokenExpired(token) && isRefreshToken(token);
            logger.debug("Refresh token validation result: {}, claims: {}", isValid, claims);
            return isValid;
        } catch (ExpiredJwtException e) {
            logger.warn("Refresh token expired: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Error validating refresh token: {}", e.getMessage());
            return false;
        }
    }

    private Boolean isAccessToken(String token) {
        String type = getClaimFromToken(token, claims -> claims.get("type", String.class));
        boolean isAccess = "access".equals(type);
        logger.trace("Token type check: {}, isAccess: {}", type, isAccess);
        return isAccess;
    }

    private Boolean isRefreshToken(String token) {
        String type = getClaimFromToken(token, claims -> claims.get("type", String.class));
        boolean isRefresh = "refresh".equals(type);
        logger.trace("Token type check: {}, isRefresh: {}", type, isRefresh);
        return isRefresh;
    }

    public Long getUserIdFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("userId", Long.class));
    }

    public String getEmailFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("email", String.class));
    }

    private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        boolean isExpired = expiration.before(new Date());
        logger.trace("Token expiration check: {}, isExpired: {}", expiration, isExpired);
        return isExpired;
    }

    private Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }
}
