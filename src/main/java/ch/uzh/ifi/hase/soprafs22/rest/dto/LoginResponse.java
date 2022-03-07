package ch.uzh.ifi.hase.soprafs22.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginResponse {
  private long userId;

  @JsonProperty("access_token")
  private String accessToken;

  public LoginResponse(long userId, String accessToken) {
    this.userId = userId;
    this.accessToken = accessToken;
  }

  public long getUserId() {
    return userId;
  }

  public String getAccessToken() {
    return accessToken;
  }
}
