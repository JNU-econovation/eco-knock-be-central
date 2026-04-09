package jnu.econovation.ecoknockbecentral.common.security.util;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.Date;
import javax.crypto.SecretKey;
import jnu.econovation.ecoknockbecentral.member.dto.MemberInfoDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    private static final Duration EXPIRATION = Duration.ofHours(6);

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    private SecretKey key;
    private JwtParser jwtParser;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.jwtParser = Jwts.parser().verifyWith(key).build();
    }

    public Long extractId(String token) {
        try {
            String subject = jwtParser.parseSignedClaims(token).getPayload().getSubject();
            return subject != null ? Long.parseLong(subject) : null;
        } catch (JwtException | NumberFormatException e) {
            return null;
        }
    }

    public String generateToken(MemberInfoDTO memberInfo) {
        long now = System.currentTimeMillis();

        return Jwts.builder()
                .subject(String.valueOf(memberInfo.getId()))
                .claim("id", memberInfo.getId())
                .issuedAt(new Date(now))
                .expiration(new Date(now + EXPIRATION.toMillis()))
                .signWith(key)
                .compact();
    }

    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        try {
            jwtParser.parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}