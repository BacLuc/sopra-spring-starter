package ch.uzh.ifi.hase.soprafs22.rest.mapper;

import ch.uzh.ifi.hase.soprafs22.entity.User;
import ch.uzh.ifi.hase.soprafs22.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs22.rest.dto.UserPostDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * DTOMapper This class is responsible for generating classes that will automatically transform/map
 * the internal representation of an entity (e.g., the User) to the external/API representation
 * (e.g., UserGetDTO for getting, UserPostDTO for creating) and vice versa. Additional mappers can
 * be defined for new entities. Always created one mapper for getting information (GET) and one
 * mapper for creating information (POST).
 */
@Mapper
public interface DTOMapper {

  DTOMapper INSTANCE = Mappers.getMapper(DTOMapper.class);

  @Mapping(source = "name", target = "name")
  @Mapping(source = "username", target = "username")
  @Mapping(source = "password", target = "passwordHash")
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "token", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "birthday", ignore = true)
  @Mapping(target = "created", ignore = true)
  User convertUserPostDTOtoEntity(UserPostDTO userPostDTO);

  @Mapping(source = "id", target = "id")
  @Mapping(source = "name", target = "name")
  @Mapping(source = "username", target = "username")
  @Mapping(source = "birthday", target = "birthday")
  @Mapping(
      target = "online",
      expression = "java(user.getStatus() == ch.uzh.ifi.hase.soprafs22.constant.UserStatus.ONLINE)")
  @Mapping(source = "created", target = "created")
  UserGetDTO convertEntityToUserGetDTO(User user);
}
