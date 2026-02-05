package com.anhvt.epms.procurement.service.impl;

import com.anhvt.epms.procurement.entity.User;
import com.anhvt.epms.procurement.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Implementation of JwtService
 * Handles JWT token operations including generation, validation, and claims extraction
 */
@Service
@Slf4j
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.signer-key}")
    private String signerKey;

    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * Generate JWT token for authenticated user
     * Includes user roles for authorization purposes
     * @param user the authenticated user
     * @return JWT token string
     */
    @Override
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("fullName", user.getFullName());
        
        // Add roles to JWT claims for @PreAuthorize authorization
        // Extract role names from User's roles collection
        claims.put("roles", user.getRoles().stream()
                .map(role -> role.getName())
                .toList());
        
        return Jwts.builder()
                .claims(claims)
                .subject(user.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey())
                .compact();
    }

    /**
     * Extract username from JWT token
     * @param token the JWT token
     * @return username
     */
    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Validate JWT token
     * @param token the JWT token to validate
     * @return true if token is valid, false otherwise
     */
    @Override
    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Invalid token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extract specific claim from token
     * @param token the JWT token
     * @param claimsResolver function to extract specific claim
     * @return the extracted claim
     */
    @Override
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims from token
     * @param token the JWT token
     * @return all claims
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Check if token is expired
     * @param token the JWT token
     * @return true if expired, false otherwise
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extract expiration date from token
     * @param token the JWT token
     * @return expiration date
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Get signing key for JWT
     * @return SecretKey for signing
     */
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(signerKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
