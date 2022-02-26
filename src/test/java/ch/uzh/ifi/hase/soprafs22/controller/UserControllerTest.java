package ch.uzh.ifi.hase.soprafs22.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.uzh.ifi.hase.soprafs22.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs22.entity.User;
import ch.uzh.ifi.hase.soprafs22.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs22.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs22.rest.dto.UserPostDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

  @MockBean private AuthHelper authHelper;

  @Autowired private UserRepository userRepository;

  @Autowired private ObjectMapper objectMapper;

  private User user1;

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();
    user1 = new User();
    user1.setName("Firstname Lastname");
    user1.setUsername("firstname@lastname");
    user1.setStatus(UserStatus.OFFLINE);
    user1.setBirthday(SOME_BIRTHDAY);
    user1.setPasswordHash("hash");
    user1.setToken("token");
    userRepository.save(user1);
    userRepository.flush();

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
    List<User> allUsers = userRepository.findAll();
    // when
    MockHttpServletRequestBuilder getRequest =
        get("/users").contentType(MediaType.APPLICATION_JSON);

    // then
    mockMvc
        .perform(getRequest)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].id", is(allUsers.get(0).getId().intValue())))
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
    mockMvc.perform(get("/users/2")).andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser
  public void return_200_and_single_user_if_valid() throws Exception {
    String response =
        mockMvc
            .perform(get("/users"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    UserGetDTO[] userGetDTOs = objectMapper.readValue(response, UserGetDTO[].class);

    mockMvc
        .perform(get("/users/%s".formatted(userGetDTOs[0].getId())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name", is(user1.getName())))
        .andExpect(jsonPath("$.username", is(user1.getUsername())))
        .andExpect(jsonPath("$.status", is(user1.getStatus().toString())))
        .andExpect(jsonPath("$.birthday", is(user1.getBirthday().toString())));
  }

  @Test
  public void createUser_validInput_userCreated() throws Exception {
    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setName("Test User");
    userPostDTO.setUsername("testUsername");
    userPostDTO.setPassword("test");

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest =
        post("/users").contentType(MediaType.APPLICATION_JSON).content(asJsonString(userPostDTO));

    // then
    mockMvc
        .perform(postRequest)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(3)))
        .andExpect(jsonPath("$.name", is(userPostDTO.getName())))
        .andExpect(jsonPath("$.username", is(userPostDTO.getUsername())))
        .andExpect(jsonPath("$.status", is(UserStatus.OFFLINE.name())));
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
