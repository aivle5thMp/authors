package mp.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtVerifier {

    private static final Logger logger = LoggerFactory.getLogger(JwtVerifier.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    public Map<String, Object> verifyAndExtract(String token) {
        logger.info("JWT 토큰 검증 시작: {}", token.substring(0, Math.min(20, token.length())) + "...");
        logger.info("JWT Secret 길이: {}", jwtSecret.length());
        logger.info("JWT Secret (처음 20자): {}", jwtSecret.substring(0, Math.min(20, jwtSecret.length())) + "...");
        
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // 페이로드에서 모든 정보 추출 (JwtUtil의 generateToken과 일치)
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("user_id", claims.get("sub"));  // sub 클레임에서 user_id 추출
            userInfo.put("role", claims.get("role"));
            userInfo.put("is_subscribed", claims.get("is_subscribed"));
            userInfo.put("issued_at", claims.getIssuedAt());
            userInfo.put("expires_at", claims.getExpiration());

            logger.info("JWT 토큰 검증 성공 - 사용자 ID: {}, 역할: {}", 
                userInfo.get("user_id"), userInfo.get("role"));
            
            return userInfo;
            
        } catch (ExpiredJwtException e) {
            logger.error("JWT 토큰 만료: {}", e.getMessage());
            throw new RuntimeException("토큰이 만료되었습니다. 만료시간: " + e.getClaims().getExpiration());
            
        } catch (UnsupportedJwtException e) {
            logger.error("지원되지 않는 JWT 토큰 형식: {}", e.getMessage());
            throw new RuntimeException("지원되지 않는 토큰 형식입니다.");
            
        } catch (MalformedJwtException e) {
            logger.error("잘못된 JWT 토큰 구조: {}", e.getMessage());
            throw new RuntimeException("잘못된 토큰 구조입니다.");
            
        } catch (SignatureException e) {
            logger.error("JWT 서명 검증 실패: {}", e.getMessage());
            throw new RuntimeException("토큰 서명이 유효하지 않습니다.");
            
        } catch (IllegalArgumentException e) {
            logger.error("JWT 토큰이 null이거나 빈 문자열: {}", e.getMessage());
            throw new RuntimeException("토큰이 제공되지 않았습니다.");
            
        } catch (Exception e) {
            logger.error("JWT 토큰 검증 중 예상치 못한 오류: {}", e.getMessage(), e);
            throw new RuntimeException("토큰 검증 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}