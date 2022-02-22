package ch.uzh.ifi.hase.soprafs22.controller;

import ch.uzh.ifi.hase.soprafs22.rest.dto.LoginDTO;
import ch.uzh.ifi.hase.soprafs22.security.jwt.JwtUtil;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
public class LoginController {
  private final AuthenticationManager authenticationManager;
  private final JwtUtil jwtUtil;

  @Autowired
  public LoginController(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
    this.authenticationManager = authenticationManager;
    this.jwtUtil = jwtUtil;
  }

  @PostMapping("/login")
  public ResponseEntity<Void> login(@Valid @RequestBody LoginDTO loginDTO) {
    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginDTO.getUsername(), loginDTO.getPassword()));
    SecurityContextHolder.getContext().setAuthentication(authentication);
    UserDetails principal = (UserDetails) authentication.getPrincipal();
    ResponseCookie responseCookie = jwtUtil.generateJwtCookie(principal);
    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, responseCookie.toString()).build();
  }
}
