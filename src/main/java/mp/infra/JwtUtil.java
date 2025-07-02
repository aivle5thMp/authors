package mp.infra;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {
    
    private final SecretKey secretKey;
    
    @Value("${jwt.expiration:86400000}")
    private long expirationTime; // 기본값: 24시간 (밀리초)
    
    public JwtUtil(@Value("${jwt.secret}") String jwtSecret) {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
    
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    public UUID extractUserId(String token) {
        String userIdString = extractAllClaims(token).get("sub", String.class);
        return UUID.fromString(userIdString);
    }
    
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }
    
    public Boolean extractIsSubscribed(String token) {
        return extractAllClaims(token).get("is_subscribed", Boolean.class);
    }
    
    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }
    
    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    public Boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
} 