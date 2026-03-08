package com.medicalcrm.backend.security;

import com.medicalcrm.backend.model.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class JwtService {

    private static final String SECRET =
            "THIS_IS_A_SECRET_KEY_FOR_MEDICAL_CRM_PROJECT_1234567890";
    private static final long EXPIRATION = 1000 * 60 * 60 * 24; // 24 ώρες
    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    public String generateToken(String username, Role role) {
        String token = Jwts.builder()
                .setSubject(username)
                .claim("role", "ROLE_" + role.name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        log.info("Generated token: {}", token);

        return token;
    }


    public String extractUsername(String token) {
        return parseToken(token).getBody().getSubject();
    }

    public Role extractRole(String token) {
        String roleName = parseToken(token).getBody().get("role", String.class);
        return Role.valueOf(roleName.replace("ROLE_", "")); 
    }

    public boolean isTokenValid(String token, String username) {
        return extractUsername(token).equals(username)
                && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return parseToken(token).getBody().getExpiration().before(new Date());
    }

    private Jws<Claims> parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }

}
