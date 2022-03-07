package ch.uzh.ifi.hase.soprafs22.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import ch.uzh.ifi.hase.soprafs22.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs22.entity.User;
import ch.uzh.ifi.hase.soprafs22.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs22.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs22.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs22.rest.dto.UserPutDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
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
  private static final LocalDate ANOTHER_BIRTHDAY = LocalDate.parse("2022-01-03");

  @Autowired private MockMvc mockMvc;

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
  }

  @Test
  public void httpstatus_401_for_get_users_when_not_logged_in() throws Exception {
    mockMvc.perform(get("/users")).andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser
  public void get_users_validates_accept_header() throws Exception {
    mockMvc.perform(get("/users").accept(IMAGE_GIF)).andExpect(status().isNotAcceptable());
  }

  @Test
  @WithMockUser
  public void givenUsers_whenGetUsers_thenReturnJsonArray() throws Exception {
    List<User> allUsers = userRepository.findAll();
    // when
    MockHttpServletRequestBuilder getRequest = get("/users").accept(APPLICATION_JSON);

    // then
    mockMvc
        .perform(getRequest)
        .andExpect(status().isOk())
        .andExpect(header().string(CONTENT_TYPE, APPLICATION_JSON.toString()))
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].id", is(allUsers.get(0).getId().intValue())))
        .andExpect(jsonPath("$[0].name", is(user1.getName())))
        .andExpect(jsonPath("$[0].username", is(user1.getUsername())))
        .andExpect(jsonPath("$[0].logged_in", is(false)))
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
  public void get_single_user_validates_accept_header() throws Exception {
    mockMvc
        .perform(get("/users/%s".formatted(user1.getId())).accept(IMAGE_GIF))
        .andExpect(status().isNotAcceptable());
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
        .andExpect(header().string(CONTENT_TYPE, APPLICATION_JSON.toString()))
        .andExpect(jsonPath("$.name", is(user1.getName())))
        .andExpect(jsonPath("$.username", is(user1.getUsername())))
        .andExpect(jsonPath("$.logged_in", is(false)))
        .andExpect(jsonPath("$.birthday", is(user1.getBirthday().toString())));
  }

  @Test
  public void createUser_validates_contentType() throws Exception {
    mockMvc
        .perform(post("/users").accept(APPLICATION_JSON).content(APPLICATION_XML_VALUE))
        .andExpect(status().isUnsupportedMediaType());
  }

  @Test
  public void createUser_validates_accept() throws Exception {
    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setName("Test User");
    userPostDTO.setUsername("testUsername");
    userPostDTO.setPassword("test");
    mockMvc
        .perform(
            post("/users")
                .accept(IMAGE_GIF)
                .contentType(APPLICATION_JSON)
                .content(asJsonString(userPostDTO)))
        .andExpect(status().isNotAcceptable());
  }

  @Test
  public void createUser_validInput_userCreated() throws Exception {
    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setName("Test User");
    userPostDTO.setUsername("testUsername");
    userPostDTO.setPassword("test");

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest =
        post("/users")
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .content(asJsonString(userPostDTO));

    // then
    mockMvc
        .perform(postRequest)
        .andExpect(status().isCreated())
        .andExpect(header().string(CONTENT_TYPE, APPLICATION_JSON.toString()))
        .andExpect(jsonPath("$.id", is(notNullValue())))
        .andExpect(jsonPath("$.name", is(userPostDTO.getName())))
        .andExpect(jsonPath("$.username", is(userPostDTO.getUsername())))
        .andExpect(jsonPath("$.logged_in", is(true)))
        .andExpect(jsonPath("$.creation_date", is(notNullValue())));
  }

  @Test
  public void createUser_rejects_existing_user_name() throws Exception {
    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setName("Test User");
    userPostDTO.setUsername(user1.getUsername());
    userPostDTO.setPassword("test");

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest =
        post("/users")
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .content(asJsonString(userPostDTO));

    // then
    mockMvc.perform(postRequest).andExpect(status().isConflict());
  }

  @Test
  public void httpstatus_401_for_put_user_item_when_not_logged_in() throws Exception {
    mockMvc
        .perform(put("/users/1").accept(APPLICATION_JSON).contentType(APPLICATION_JSON).content(""))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser
  public void httpstatus_404_for_put_user_item_when_not_found() throws Exception {
    UserPutDTO userPutDTO = new UserPutDTO();
    mockMvc
        .perform(
            put("/users/100004").contentType(APPLICATION_JSON).content(asJsonString(userPutDTO)))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser
  public void update_username_and_birthday_if_both_given() throws Exception {
    List<User> users = userRepository.findAll();
    User firstUser = users.get(0);
    String newUserName = "new name";
    UserPutDTO userPutDTO = new UserPutDTO(newUserName, ANOTHER_BIRTHDAY);
    mockMvc
        .perform(
            put("/users/%s".formatted(firstUser.getId()))
                .contentType(APPLICATION_JSON)
                .content(asJsonString(userPutDTO)))
        .andExpect(status().isNoContent());

    User updatedUser = userRepository.findById(firstUser.getId()).orElseThrow();
    assertThat(updatedUser.getUsername(), is(newUserName));
    assertThat(updatedUser.getBirthday(), is(ANOTHER_BIRTHDAY));
  }

  @Test
  @WithMockUser
  public void update_only_birthday() throws Exception {
    List<User> users = userRepository.findAll();
    User firstUser = users.get(0);
    UserPutDTO userPutDTO = new UserPutDTO(null, ANOTHER_BIRTHDAY);
    mockMvc
        .perform(
            put("/users/%s".formatted(firstUser.getId()))
                .contentType(APPLICATION_JSON)
                .content(asJsonString(userPutDTO)))
        .andExpect(status().isNoContent());

    User updatedUser = userRepository.findById(firstUser.getId()).orElseThrow();
    assertThat(updatedUser.getUsername(), is(notNullValue()));
    assertThat(updatedUser.getBirthday(), is(ANOTHER_BIRTHDAY));
  }

  @Test
  @WithMockUser
  public void update_only_username_if_birthday_is_null() throws Exception {
    List<User> users = userRepository.findAll();
    User firstUser = users.get(0);
    String newUserName = "new name";
    UserPutDTO userPutDTO = new UserPutDTO(newUserName, null);
    mockMvc
        .perform(
            put("/users/%s".formatted(firstUser.getId()))
                .contentType(APPLICATION_JSON)
                .content(asJsonString(userPutDTO)))
        .andExpect(status().isNoContent());

    User updatedUser = userRepository.findById(firstUser.getId()).orElseThrow();
    assertThat(updatedUser.getUsername(), is(newUserName));
    assertThat(updatedUser.getBirthday(), is(notNullValue()));
  }

  @Test
  @WithMockUser
  public void update_only_username_if_birthday_is_missing() throws Exception {
    List<User> users = userRepository.findAll();
    User firstUser = users.get(0);
    String newUserName = "new name";
    mockMvc
        .perform(
            put("/users/%s".formatted(firstUser.getId()))
                .contentType(APPLICATION_JSON)
                .content("{\"username\":\"%s\"}".formatted(newUserName)))
        .andExpect(status().isNoContent());

    User updatedUser = userRepository.findById(firstUser.getId()).orElseThrow();
    assertThat(updatedUser.getUsername(), is(newUserName));
    assertThat(updatedUser.getBirthday(), is(notNullValue()));
  }

  @Test
  @WithMockUser
  public void update_only_birthday_if_username_is_missing() throws Exception {
    List<User> users = userRepository.findAll();
    User firstUser = users.get(0);
    mockMvc
        .perform(
            put("/users/%s".formatted(firstUser.getId()))
                .contentType(APPLICATION_JSON)
                .content("{\"birthday\":\"%s\"}".formatted(ANOTHER_BIRTHDAY.toString())))
        .andExpect(status().isNoContent());

    User updatedUser = userRepository.findById(firstUser.getId()).orElseThrow();
    assertThat(updatedUser.getUsername(), is(notNullValue()));
    assertThat(updatedUser.getBirthday(), is(ANOTHER_BIRTHDAY));
  }

  @Test
  @WithMockUser
  public void update_validates_date_format() throws Exception {
    List<User> users = userRepository.findAll();
    User firstUser = users.get(0);
    mockMvc
        .perform(
            put("/users/%s".formatted(firstUser.getId()))
                .contentType(APPLICATION_JSON)
                .content("{\"birthday\":\"20-2\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  public void update_checks_content_type() throws Exception {
    List<User> users = userRepository.findAll();
    User firstUser = users.get(0);
    mockMvc
        .perform(put("/users/%s".formatted(firstUser.getId())).content("{\"birthday\":\"20-2\"}"))
        .andExpect(status().isUnsupportedMediaType());
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
