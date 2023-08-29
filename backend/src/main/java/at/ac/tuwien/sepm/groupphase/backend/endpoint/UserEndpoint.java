package at.ac.tuwien.sepm.groupphase.backend.endpoint;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedUserDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.SetNewPasswordDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.UserRegisterDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.UserUpdateDto;
import at.ac.tuwien.sepm.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepm.groupphase.backend.service.UserService;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/users")
public class UserEndpoint {

    private final UserService userService;

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public UserEndpoint(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @PermitAll
    @ResponseStatus(HttpStatus.CREATED)
    public DetailedUserDto register(@Valid @RequestBody UserRegisterDto userRegisterDto) throws ConflictException {
        LOGGER.info("POST /api/v1/users body: {}", userRegisterDto);
        return userService.register(userRegisterDto);
    }

    @PostMapping(value = "/reset-password")
    @PermitAll
    public String sendPasswordResetLink(@RequestBody String email) {
        LOGGER.info("POST /api/v1/users/reset-password body: {}", email);
        return userService.sendPasswordResetLink(email);
    }

    @PostMapping(value = "/set-new-password")
    @PermitAll
    public String setNewPassword(@RequestBody SetNewPasswordDto setNewPasswordDto) {
        LOGGER.info("POST /api/v1/users/set-new-password body: {}", setNewPasswordDto);
        return userService.setNewPassword(setNewPasswordDto.getPassword(), setNewPasswordDto.getEmail(), setNewPasswordDto.getToken());
    }


    @GetMapping(value = "/{id}")
    @Secured({"ROLE_USER"})
    public DetailedUserDto getUserById(@PathVariable UUID id) {
        LOGGER.info("GET /api/v1/users/{}", id);
        return userService.getUserById(id);
    }

    @DeleteMapping(value = "/{id}")
    @Secured({"ROLE_USER"})
    public void deleteUser(@PathVariable UUID id) {
        LOGGER.info("DELETE /api/v1/users/{}", id);
        userService.deleteUserById(id);
    }


    @PutMapping
    @Secured({"ROLE_USER"})
    public DetailedUserDto update(@Valid @RequestBody UserUpdateDto userUpdateDto) throws ConflictException {
        LOGGER.info("PUT /api/v1/users body: {}", userUpdateDto);
        return userService.updateUser(userUpdateDto);
    }

    @GetMapping
    @Secured({"ROLE_ADMIN"})
    public List<DetailedUserDto> getAllUsers(@RequestParam Boolean locked) {
        LOGGER.info("GET /api/v1/users?locked={}", locked);
        return userService.getAllUsers(locked);
    }

    @GetMapping(value = "/search")
    @Secured({"ROLE_ADMIN"})
    public List<DetailedUserDto> searchUsersWithUsernameAndLocked(@RequestParam String username, @RequestParam Boolean locked) {
        LOGGER.info("GET /api/v1/users/search?email={}&locked={}", username, locked);
        return userService.searchUsersWithUsernameAndLocked(username, locked);
    }
}
