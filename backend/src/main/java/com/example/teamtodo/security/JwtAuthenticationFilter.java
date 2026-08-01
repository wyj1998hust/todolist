package com.example.teamtodo.security;

import com.example.teamtodo.config.AppProperties;
import com.example.teamtodo.repository.UserAccountRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtService jwtService;
  private final UserAccountRepository userRepository;
  private final AppProperties properties;

  public JwtAuthenticationFilter(JwtService jwtService, UserAccountRepository userRepository, AppProperties properties) {
    this.jwtService = jwtService;
    this.userRepository = userRepository;
    this.properties = properties;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    if (SecurityContextHolder.getContext().getAuthentication() == null) {
      String token = extractCookie(request, properties.getAuth().getCookieName());
      if (token != null) {
        jwtService.verifyAndGetIdentity(token)
            .flatMap(identity -> userRepository.findById(identity.userId())
                .filter(user -> user.isActive() && user.getSessionVersion() == identity.sessionVersion()))
            .ifPresent(user -> {
              AuthenticatedUser principal = AuthenticatedUser.from(user);
              UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                  principal, null, principal.authorities());
              authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
              SecurityContextHolder.getContext().setAuthentication(authentication);
            });
      }
    }
    filterChain.doFilter(request, response);
  }

  private String extractCookie(HttpServletRequest request, String cookieName) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return null;
    }
    for (Cookie cookie : cookies) {
      if (cookieName.equals(cookie.getName())) {
        return cookie.getValue();
      }
    }
    return null;
  }
}
