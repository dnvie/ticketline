package at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedUserDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.SimpleUserDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.UserRegisterDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.UserUpdateDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.ApplicationUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;


@Mapper
public interface UserMapper {
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "counter", constant = "0")
    ApplicationUser userRegisterDtoToApplicationUser(UserRegisterDto userRegisterDto);

    @Mapping(target = "counter", constant = "0")
    ApplicationUser userUpdateDtoToApplicationUser(UserUpdateDto userUpdateDto);

    UserRegisterDto applicationUserToUserRegisterDto(ApplicationUser applicationUser);

    DetailedUserDto applicationUserToDetailedUserDto(ApplicationUser applicationUser);

    List<DetailedUserDto> applicationUserToDetailedUserDto(List<ApplicationUser> applicationUsers);

    SimpleUserDto applicationUserToSimpleUserDto(ApplicationUser applicationUser);

}
