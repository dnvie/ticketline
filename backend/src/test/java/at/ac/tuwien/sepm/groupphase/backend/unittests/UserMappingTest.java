package at.ac.tuwien.sepm.groupphase.backend.unittests;

import at.ac.tuwien.sepm.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedUserDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.UserRegisterDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.UserUpdateDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper.UserMapper;
import at.ac.tuwien.sepm.groupphase.backend.entity.ApplicationUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class UserMappingTest implements TestData {

    private final ApplicationUser user = ApplicationUser.builder()
        .id(TEST_UUID)
        .firstName(TEST_USER_FIRSTNAME)
        .lastName(TEST_USER_LASTNAME)
        .email(TEST_USER_EMAIL)
        .password(TEST_USER_PASSWORD)
        .admin(TEST_USER_ADMIN)
        .enabled(TEST_USER_ENABLED)
        .build();
    @Autowired
    private UserMapper UserMapper;

    @Test
    public void givenNothing_whenMapUserEntityToUserDto_thenUserDtoHasAllProperties() {

        DetailedUserDto detailedUserDto = UserMapper.applicationUserToDetailedUserDto(user);

        assertAll(
            () -> assertEquals(TEST_UUID, detailedUserDto.getId()),
            () -> assertEquals(TEST_USER_FIRSTNAME, detailedUserDto.getFirstName()),
            () -> assertEquals(TEST_USER_LASTNAME, detailedUserDto.getLastName()),
            () -> assertEquals(TEST_USER_EMAIL, detailedUserDto.getEmail()),
            () -> assertEquals(TEST_USER_PASSWORD, detailedUserDto.getPassword()),
            () -> assertEquals(TEST_USER_ADMIN, detailedUserDto.getAdmin()),
            () -> assertEquals(TEST_USER_ENABLED, detailedUserDto.getEnabled())
        );

    }

    @Test
    public void givenNothing_whenMapListWithTwoUserEntitiesToSimpleDto_thenGetListWithSizeTwoAndAllProperties() {
        List<ApplicationUser> users = new ArrayList<>();
        users.add(user);
        users.add(user);


        List<DetailedUserDto> simpleUserDtos = UserMapper.applicationUserToDetailedUserDto(users);
        assertAll(
            () -> assertEquals(2, simpleUserDtos.size()),
            () -> assertEquals(TEST_UUID, simpleUserDtos.get(0).getId()),
            () -> assertEquals(TEST_USER_FIRSTNAME, simpleUserDtos.get(0).getFirstName()),
            () -> assertEquals(TEST_USER_LASTNAME, simpleUserDtos.get(0).getLastName()),
            () -> assertEquals(TEST_USER_EMAIL, simpleUserDtos.get(0).getEmail()),
            () -> assertEquals(TEST_USER_PASSWORD, simpleUserDtos.get(0).getPassword()),
            () -> assertEquals(TEST_USER_ADMIN, simpleUserDtos.get(0).getAdmin()),
            () -> assertEquals(TEST_USER_ENABLED, simpleUserDtos.get(0).getEnabled()),
            () -> assertEquals(TEST_UUID, simpleUserDtos.get(1).getId()),
            () -> assertEquals(TEST_USER_FIRSTNAME, simpleUserDtos.get(1).getFirstName()),
            () -> assertEquals(TEST_USER_LASTNAME, simpleUserDtos.get(1).getLastName()),
            () -> assertEquals(TEST_USER_EMAIL, simpleUserDtos.get(1).getEmail()),
            () -> assertEquals(TEST_USER_PASSWORD, simpleUserDtos.get(1).getPassword()),
            () -> assertEquals(TEST_USER_ADMIN, simpleUserDtos.get(1).getAdmin()),
            () -> assertEquals(TEST_USER_ENABLED, simpleUserDtos.get(1).getEnabled())
        );
    }

    @Test
    public void givenNothing_whenMapUserUpdateDtoToUserEntity_thenUserEntityHasAllProperties() {

        UserUpdateDto userUpdateDto = UserUpdateDto.builder()
            .id(TEST_UUID)
            .firstName(TEST_USER_FIRSTNAME)
            .lastName(TEST_USER_LASTNAME)
            .email(TEST_USER_EMAIL)
            .password(TEST_USER_PASSWORD)
            .admin(TEST_USER_ADMIN)
            .enabled(TEST_USER_ENABLED)
            .build();


        ApplicationUser user = UserMapper.userUpdateDtoToApplicationUser(userUpdateDto);

        assertAll(
            () -> assertEquals(TEST_UUID, user.getId()),
            () -> assertEquals(TEST_USER_FIRSTNAME, user.getFirstName()),
            () -> assertEquals(TEST_USER_LASTNAME, user.getLastName()),
            () -> assertEquals(TEST_USER_EMAIL, user.getEmail()),
            () -> assertEquals(TEST_USER_PASSWORD, user.getPassword()),
            () -> assertEquals(TEST_USER_ADMIN, user.getAdmin()),
            () -> assertEquals(TEST_USER_ENABLED, user.getEnabled())
        );

    }

    @Test
    public void givenNothing_whenMapUserRegisterDtoToUserEntity_thenUserEntityHasAllProperties() {

        UserRegisterDto userRegisterDto = UserRegisterDto.builder()
            .firstName(TEST_USER_FIRSTNAME)
            .lastName(TEST_USER_LASTNAME)
            .email(TEST_USER_EMAIL)
            .password(TEST_USER_PASSWORD)
            .build();

        ApplicationUser user = UserMapper.userRegisterDtoToApplicationUser(userRegisterDto);

        assertAll(
            () -> assertEquals(TEST_USER_FIRSTNAME, user.getFirstName()),
            () -> assertEquals(TEST_USER_LASTNAME, user.getLastName()),
            () -> assertEquals(TEST_USER_EMAIL, user.getEmail()),
            () -> assertEquals(TEST_USER_PASSWORD, user.getPassword())
        );

    }


}
