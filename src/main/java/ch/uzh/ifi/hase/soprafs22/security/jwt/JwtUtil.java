package ch.uzh.ifi.hase.soprafs22.security.jwt;

import io.jsonwebtoken.*;
import java.time.Duration;
import java.util.Date;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

@Component
public class JwtUtil {
  private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

  @Value("${jwt.cookie.name}")
  private String jwtCookieName;

  @Value("${jwt.secret}")
  private String jwtSecret;

  private long jwtExpirationMs = Duration.ofDays(1).getSeconds() * 1000;

  public String getJwtFromCookies(HttpServletRequest request) {
    Cookie cookie = WebUtils.getCookie(request, jwtCookieName);
    if (cookie != null) {
      return cookie.getValue();
    } else {
      return null;
    }
  }

  public ResponseCookie generateJwtCookie(UserDetails userPrincipal) {
    String jwt = generateTokenFromUsername(userPrincipal.getUsername());
    return ResponseCookie.from(jwtCookieName, jwt).maxAge(24 * 60 * 60).httpOnly(false).build();
  }

  public ResponseCookie getCleanJwtCookie() {
    return ResponseCookie.from(jwtCookieName, "").build();
  }

  public String getUserIdFromJwtToken(String token) {
    return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().getSubject();
  }

  public boolean validateJwtToken(String authToken) {
    try {
      Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
      return true;
    } catch (SignatureException e) {
      log.error("Invalid JWT signature: {}", e.getMessage());
    } catch (MalformedJwtException e) {
      log.error("Invalid JWT token: {}", e.getMessage());
    } catch (ExpiredJwtException e) {
      log.error("JWT token is expired: {}", e.getMessage());
    } catch (UnsupportedJwtException e) {
      log.error("JWT token is unsupported: {}", e.getMessage());
    } catch (IllegalArgumentException e) {
      log.error("JWT claims string is empty: {}", e.getMessage());
    }
    return false;
  }

  public String generateTokenFromUsername(String userId) {
    return Jwts.builder()
        .setSubject(userId)
        .setIssuedAt(new Date())
        .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
        .signWith(SignatureAlgorithm.HS512, jwtSecret)
        .compact();
  }
}
