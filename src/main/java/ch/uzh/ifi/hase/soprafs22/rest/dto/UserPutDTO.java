package ch.uzh.ifi.hase.soprafs22.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

public class UserPutDTO {
  public UserPutDTO() {}

  public UserPutDTO(String username, LocalDate birthday) {
    this.username = username;
    this.birthday = birthday;
  }

  private String username;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  private LocalDate birthday;

  public String getUsername() {
    return username;
  }

  public LocalDate getBirthday() {
    return birthday;
  }
}
