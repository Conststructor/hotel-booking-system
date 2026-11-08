package com.hotel.booking.service.security;

import com.hotel.booking.service.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Provider для работы с JWT токенами.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.issuer}")
    private String issuer;

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Генерирует JWT токен для пользователя.
     */
    public String generateToken(User user) {
        return generateToken(new Date(), user);
    }

    /**
     * Генерирует JWT токен с кастомным временем истечения.
     */
    public String generateToken(Date date, User user) {
        log.info("Generating JWT token for user: {}", user.getUsername());

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        claims.put("username", user.getUsername());

        return Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(date)
                .setIssuer(issuer)
                .setExpiration(new Date(date.getTime() + expiration * 1000))
                .claim("roles", user.getRole())
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    /**
     * Извлекает клкеймы из JWT токена.
     */
    private Claims getClaims (String token){
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Извлекает имя пользователя из JWT токена.
     */
    public String getUserNameFromJwtToken(String token) {
        Claims claims = getClaims(token);
        return claims.getSubject();
    }

    /**
     * Валидирует JWT токен.
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = getClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Загружает UserDetails по имени пользователя.
     */
    public UserDetails loadUserByUsername(String username) {
        // Для упрощения возвращаем mock-объект
        return new org.springframework.security.core.userdetails.User(
                username,
                "",
                true,
                true,
                true,
                true,
                getAuthoritiesFromToken(username)
        );
    }

    /**
     * Извлекает роли пользователя из токена.
     */
    private List<GrantedAuthority> getAuthoritiesFromToken(String username) {
        return Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));
    }
}
