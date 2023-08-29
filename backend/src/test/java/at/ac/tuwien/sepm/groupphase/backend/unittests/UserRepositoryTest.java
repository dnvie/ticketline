package at.ac.tuwien.sepm.groupphase.backend.unittests;

import at.ac.tuwien.sepm.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepm.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepm.groupphase.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
// This test slice annotation is used instead of @SpringBootTest to load only repository beans instead of
// the entire application context
@DataJpaTest
@ActiveProfiles("test")
public class UserRepositoryTest implements TestData {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void givenNothing_whenSaveUser_thenFindListWithOneElementAndFindUserById() {
        ApplicationUser user = ApplicationUser.builder()
            .firstName("Test")
            .lastName("User")
            .email("test@email.com")
            .password("password")
            .admin(false)
            .enabled(true)
            .counter(0)
            .build();

        userRepository.save(user);

        assertAll(
            () -> assertEquals(1, userRepository.findAll().size()),
            () -> assertNotNull(userRepository.findById(user.getId()))
        );
    }

    @Test
    public void givenNothing_whenSaveUser_thenFindListWithOneElementAndFindUserByEmail() {
        ApplicationUser user = ApplicationUser.builder()
            .firstName("Test2")
            .lastName("User2")
            .email("test2@email.com")
            .password("password2")
            .admin(false)
            .enabled(true)
            .counter(0)
            .build();

        userRepository.save(user);

        assertAll(
            () -> assertEquals(1, userRepository.findAll().size()),
            () -> assertNotNull(userRepository.findUserByEmail(user.getEmail()))
        );
    }

    @Test
    public void givenNothing_whenSaveUser_thenFindListWithOneElementAndDeleteUserById() {
        ApplicationUser user = ApplicationUser.builder()
            .firstName("Test3")
            .lastName("User3")
            .email("test3@email.com")
            .password("password3")
            .admin(false)
            .enabled(true)
            .counter(0)
            .build();

        userRepository.save(user);

        assertAll(
            () -> assertEquals(1, userRepository.findAll().size()),
            () -> assertNotNull(userRepository.findUserById(user.getId()))
        );

        assertEquals(userRepository.deleteByUuid(user.getId()), 1);
        assertAll(
            () -> assertEquals(0, userRepository.findAll().size()),
            () -> assertEquals("[]", userRepository.findUserById(user.getId()).toString())
        );
    }


    @Test

    public void givenNothing_whenSaveUsers_thenFindListWithOneLockedUser() {
        ApplicationUser user = ApplicationUser.builder()
            .firstName("Test4")
            .lastName("User4")
            .email("test@email.com")
            .password("password")
            .admin(false)
            .enabled(false)
            .counter(0)
            .build();

        userRepository.save(user);

        ApplicationUser user2 = ApplicationUser.builder()
            .firstName("Test2")
            .lastName("User2")
            .email("test2@email.com")
            .password("password2")
            .admin(false)
            .enabled(true)
            .counter(0)
            .build();

        userRepository.save(user2);

        assertAll(
            () -> assertEquals(2, userRepository.findAll().size()),
            () -> assertEquals(1, userRepository.findAllByEnabledIsFalse().size()),
            () -> assertEquals(user.getEmail(), userRepository.findAllByEnabledIsFalse().get(0).getEmail())
        );


    }
}
