package com.example.teamtodo.security;

import com.example.teamtodo.config.AppProperties;
import com.example.teamtodo.domain.UserRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JwtServiceTest {
  @Test
  void issuesAndVerifiesHttpOnlyCookieTokenPayload() {
    AppProperties properties = new AppProperties();
    properties.getAuth().setJwtSecret("a-development-test-secret-that-is-longer-than-thirty-two-bytes");
    properties.getAuth().setSessionHours(1);
    JwtService service = new JwtService(properties);

    String token = service.issueToken(new AuthenticatedUser(42L, "member1", "成员 1", UserRole.MEMBER));

    assertEquals(42L, service.verifyAndGetUserId(token).orElseThrow());
  }
}
