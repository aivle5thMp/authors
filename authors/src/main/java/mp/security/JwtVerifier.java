package mp.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtVerifier {

    @Value("${jwt.secret}")
    private String jwtSecret;

    public Map<String, Object> verifyAndExtract(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // 페이로드에서 모든 정보 추출
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("user_id", claims.getSubject());
            userInfo.put("name", claims.get("name"));
            userInfo.put("email", claims.get("email"));
            userInfo.put("role", claims.get("role"));
            userInfo.put("is_subscribed", claims.get("is_subscribed"));

            return userInfo;
        } catch (Exception e) {
            throw new RuntimeException("Invalid token");
        }
    }
}
