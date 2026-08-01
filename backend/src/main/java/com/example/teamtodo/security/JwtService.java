package com.example.teamtodo.security;

import com.example.teamtodo.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtService {
  private final AppProperties properties;
  private final SecretKey signingKey;

  public JwtService(AppProperties properties) {
    this.properties = properties;
    String secret = properties.getAuth().getJwtSecret();
    if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
      throw new IllegalStateException("APP_AUTH_JWT_SECRET 至少需要32个字节");
    }
    this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  public String issueToken(AuthenticatedUser user) {
    Instant now = Instant.now();
    Instant expiresAt = now.plus(properties.getAuth().getSessionHours(), ChronoUnit.HOURS);
    return Jwts.builder()
        .subject(user.id().toString())
        .claim("username", user.username())
        .claim("sessionVersion", user.sessionVersion())
        .issuedAt(Date.from(now))
        .expiration(Date.from(expiresAt))
        .signWith(signingKey)
        .compact();
  }

  public Optional<Long> verifyAndGetUserId(String token) {
    return verifyAndGetIdentity(token).map(JwtIdentity::userId);
  }

  public Optional<JwtIdentity> verifyAndGetIdentity(String token) {
    try {
      Claims claims = Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
      Number sessionVersion = claims.get("sessionVersion", Number.class);
      if (sessionVersion == null) {
        return Optional.empty();
      }
      return Optional.of(new JwtIdentity(Long.parseLong(claims.getSubject()), sessionVersion.longValue()));
    } catch (RuntimeException exception) {
      return Optional.empty();
    }
  }

  public record JwtIdentity(Long userId, long sessionVersion) {}
}
