package ch.uzh.ifi.hase.soprafs22.controller;

import ch.uzh.ifi.hase.soprafs22.entity.User;
import ch.uzh.ifi.hase.soprafs22.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs22.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs22.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs22.service.UserService;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * User Controller This class is responsible for handling all REST request that are related to the
 * user. The controller will receive the request and delegate the execution to the UserService and
 * finally return the result.
 */
@RestController
public class UserController {

  private final PasswordEncoder passwordEncoder;

  private final UserService userService;

  private final AuthHelper authHelper;

  @Autowired
  UserController(PasswordEncoder passwordEncoder, UserService userService, AuthHelper authHelper) {
    this.passwordEncoder = passwordEncoder;
    this.userService = userService;
    this.authHelper = authHelper;
  }

  @GetMapping("/users")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @PreAuthorize("hasRole('USER')")
  public List<UserGetDTO> getAllUsers() {
    // fetch all users in the internal representation
    List<User> users = userService.getUsers();
    List<UserGetDTO> userGetDTOs = new ArrayList<>();

    // convert each user to the API representation
    for (User user : users) {
      userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));
    }
    return userGetDTOs;
  }

  @PostMapping("/users")
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseEntity<UserGetDTO> createUser(@Valid @RequestBody UserPostDTO userPostDTO) {
    String loginPassword = userPostDTO.getPassword();
    String hashedPassword = passwordEncoder.encode(loginPassword);
    userPostDTO.setPassword(hashedPassword);
    // convert API user to internal representation
    User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

    // create user
    User createdUser = userService.createUser(userInput);

    // convert internal representation of user back to API
    UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(createdUser);
    ResponseCookie cookie = authHelper.createCookieFor(userPostDTO.getUsername(), loginPassword);
    return ResponseEntity.status(HttpStatus.CREATED)
        .header(HttpHeaders.SET_COOKIE, cookie.toString())
        .body(userGetDTO);
  }
}
