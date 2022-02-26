package ch.uzh.ifi.hase.soprafs22.controller;

import ch.uzh.ifi.hase.soprafs22.entity.User;
import ch.uzh.ifi.hase.soprafs22.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs22.rest.dto.LoginDTO;
import ch.uzh.ifi.hase.soprafs22.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs22.rest.mapper.DTOMapper;
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
  private final UserRepository userRepository;

  @Autowired
  public LoginController(AuthHelper authHelper, UserRepository userRepository) {
    this.authHelper = authHelper;
    this.userRepository = userRepository;
  }

  @PostMapping("/login")
  public ResponseEntity<UserGetDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
    User byUsername = userRepository.findByUsername(loginDTO.getUsername());
    UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(byUsername);
    ResponseCookie responseCookie =
        authHelper.createCookieFor(byUsername.getId().toString(), loginDTO.getPassword());
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
        .body(userGetDTO);
  }
}
