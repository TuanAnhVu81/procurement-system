package com.anhvt.epms.procurement.service;

import com.anhvt.epms.procurement.entity.User;

import java.util.function.Function;
import io.jsonwebtoken.Claims;

/**
 * Service interface for JWT token operations
 * Handles token generation, validation, and claims extraction
 */
public interface JwtService {
    
    /**
     * Generate JWT token for authenticated user
     * @param user the authenticated user
     * @return JWT token string
     */
    String generateToken(User user);
    
    /**
     * Extract username from JWT token
     * @param token the JWT token
     * @return username
     */
    String extractUsername(String token);
    
    /**
     * Validate JWT token
     * @param token the JWT token to validate
     * @return true if token is valid, false otherwise
     */
    boolean isTokenValid(String token);
    
    /**
     * Extract specific claim from token
     * @param token the JWT token
     * @param claimsResolver function to extract specific claim
     * @return the extracted claim
     */
    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);
}
