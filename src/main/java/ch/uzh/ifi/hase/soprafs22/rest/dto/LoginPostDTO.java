package ch.uzh.ifi.hase.soprafs22.rest.dto;

import javax.validation.constraints.NotBlank;

public class LoginPostDTO {
  @NotBlank private String username;

  @NotBlank private String password;

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}
