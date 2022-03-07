package ch.uzh.ifi.hase.soprafs22.security.jwt;

import ch.uzh.ifi.hase.soprafs22.security.Authorities;
import ch.uzh.ifi.hase.soprafs22.service.UserDetailServiceImpl;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {
  private static final Logger log = LoggerFactory.getLogger(AuthTokenFilter.class);

  private final JwtUtil jwtUtil;

  private final UserDetailServiceImpl userDetailService;

  @Autowired
  public AuthTokenFilter(JwtUtil jwtUtil, UserDetailServiceImpl userDetailService) {
    this.jwtUtil = jwtUtil;
    this.userDetailService = userDetailService;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (authorization != null) {
      validateAuthorizationHeader(request, authorization);
    } else {
      log.warn("not authenticated");
    }
    filterChain.doFilter(request, response);
  }

  private void validateAuthorizationHeader(HttpServletRequest request, String authorizationHeader) {
    String[] parts = authorizationHeader.split(" ");
    if (parts.length != 2) {
      return;
    }
    if (!parts[0].equals("Bearer")) {
      return;
    }
    String token = parts[1];
    if (!jwtUtil.validateJwtToken(token)) {
      return;
    }
    String userId = jwtUtil.getUserIdFromJwtToken(token);
    UserDetails userDetails = userDetailService.loadUserByUsername(userId);
    Optional.ofNullable(userDetails)
        .orElseThrow(() -> new UsernameNotFoundException("User %s not found".formatted(userId)));
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(
            userDetails, null, List.of(new SimpleGrantedAuthority(Authorities.ROLE_USER.name())));
    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }
}
