package ch.uzh.ifi.hase.soprafs22.controller;

import ch.uzh.ifi.hase.soprafs22.rest.dto.LoginDTO;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {
  private final AuthHelper authHelper;

  @Autowired
  public LoginController(AuthHelper authHelper) {
    this.authHelper = authHelper;
  }

  @PostMapping("/login")
  public ResponseEntity<Void> login(@Valid @RequestBody LoginDTO loginDTO) {
    ResponseCookie responseCookie =
        authHelper.createCookieFor(loginDTO.getUsername(), loginDTO.getPassword());
    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, responseCookie.toString()).build();
  }
}
