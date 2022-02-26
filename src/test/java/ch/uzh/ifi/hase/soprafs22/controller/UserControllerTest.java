package ch.uzh.ifi.hase.soprafs22.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.uzh.ifi.hase.soprafs22.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs22.entity.User;
import ch.uzh.ifi.hase.soprafs22.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs22.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

/**
 * UserControllerTest This is a WebMvcTest which allows to test the UserController i.e. GET/POST
 * request without actually sending them over the network. This tests if the UserController works.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {
  private static final LocalDate SOME_BIRTHDAY = LocalDate.parse("2022-01-02");

  @Autowired private MockMvc mockMvc;

  @MockBean private UserService userService;

  @MockBean private AuthHelper authHelper;

  @Autowired private ObjectMapper objectMapper;

  private User user1;

  @BeforeEach
  void setUp() {
    user1 = new User();
    user1.setName("Firstname Lastname");
    user1.setUsername("firstname@lastname");
    user1.setStatus(UserStatus.OFFLINE);
    user1.setBirthday(SOME_BIRTHDAY);

    when(authHelper.createCookieFor(notNull(), notNull()))
        .thenReturn(ResponseCookie.from("jwtCookieName", "jwt").build());
  }

  @Test
  public void httpstatus_401_for_get_users_when_not_logged_in() throws Exception {
    mockMvc.perform(get("/users")).andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser
  public void givenUsers_whenGetUsers_thenReturnJsonArray() throws Exception {
    given(userService.getUsers()).willReturn(List.of(user1));

    // when
    MockHttpServletRequestBuilder getRequest =
        get("/users").contentType(MediaType.APPLICATION_JSON);

    // then
    mockMvc
        .perform(getRequest)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].name", is(user1.getName())))
        .andExpect(jsonPath("$[0].username", is(user1.getUsername())))
        .andExpect(jsonPath("$[0].status", is(user1.getStatus().toString())))
        .andExpect(jsonPath("$[0].birthday", is(user1.getBirthday().toString())));
  }

  @Test
  public void httpstatus_401_for_get_user_item_when_not_logged_in() throws Exception {
    mockMvc.perform(get("/users/1")).andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser
  public void httpstatus_404_for_get_user_item_when_not_found() throws Exception {
    mockMvc.perform(get("/users/1")).andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser
  public void return_200_and_single_user() throws Exception {
    given(userService.getById(1L)).willReturn(Optional.of(user1));

    mockMvc
        .perform(get("/users/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name", is(user1.getName())))
        .andExpect(jsonPath("$.username", is(user1.getUsername())))
        .andExpect(jsonPath("$.status", is(user1.getStatus().toString())))
        .andExpect(jsonPath("$.birthday", is(user1.getBirthday().toString())));
  }

  @Test
  public void createUser_validInput_userCreated() throws Exception {
    // given
    User user = new User();
    user.setId(1L);
    user.setName("Test User");
    user.setUsername("testUsername");
    user.setToken("1");
    user.setStatus(UserStatus.ONLINE);

    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setName("Test User");
    userPostDTO.setUsername("testUsername");
    userPostDTO.setPassword("test");

    given(userService.createUser(Mockito.any())).willReturn(user);

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest =
        post("/users").contentType(MediaType.APPLICATION_JSON).content(asJsonString(userPostDTO));

    // then
    mockMvc
        .perform(postRequest)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(user.getId().intValue())))
        .andExpect(jsonPath("$.name", is(user.getName())))
        .andExpect(jsonPath("$.username", is(user.getUsername())))
        .andExpect(jsonPath("$.status", is(user.getStatus().toString())));
  }

  /**
   * Helper Method to convert userPostDTO into a JSON string such that the input can be processed
   * Input will look like this: {"name": "Test User", "username": "testUsername"}
   *
   * @param object
   * @return string
   */
  private String asJsonString(final Object object) {
    try {
      return objectMapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          String.format("The request body could not be created.%s", e.toString()));
    }
  }
}
