package ch.uzh.ifi.hase.soprafs22.controller;

import ch.uzh.ifi.hase.soprafs22.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs22.entity.User;
import ch.uzh.ifi.hase.soprafs22.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs22.rest.dto.LoginDTO;
import ch.uzh.ifi.hase.soprafs22.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs22.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs22.service.UserService;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

@RestController
public class LoginController {
  private final AuthHelper authHelper;
  private final UserRepository userRepository;
  private final UserService userService;

  @Autowired
  public LoginController(
      AuthHelper authHelper, UserRepository userRepository, UserService userService) {
    this.authHelper = authHelper;
    this.userRepository = userRepository;
    this.userService = userService;
  }

  @PostMapping("/login")
  public ResponseEntity<UserGetDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
    User byUsername = userRepository.findByUsername(loginDTO.getUsername());
    byUsername.setStatus(UserStatus.ONLINE);
    userService.saveUser(byUsername);
    UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(byUsername);
    ResponseCookie responseCookie =
        authHelper.createCookieFor(byUsername.getId().toString(), loginDTO.getPassword());

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
        .body(userGetDTO);
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserDetails principal = (UserDetails) authentication.getPrincipal();
    User user =
        userService
            .getById(Long.valueOf(principal.getUsername()))
            .orElseThrow(() -> new HttpClientErrorException(HttpStatus.UNAUTHORIZED));
    user.setStatus(UserStatus.OFFLINE);
    userService.saveUser(user);
    ResponseCookie cleanCookie = authHelper.createCleanCookie();
    return ResponseEntity.noContent()
        .header(HttpHeaders.SET_COOKIE, cleanCookie.toString())
        .build();
  }
}
