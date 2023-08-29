package at.ac.tuwien.sepm.groupphase.backend.service;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedUserDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.UserLoginDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.UserRegisterDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.UserUpdateDto;
import at.ac.tuwien.sepm.groupphase.backend.exception.ConflictException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.UUID;

public interface UserService extends UserDetailsService {

    /**
     * Find a user in the context of Spring Security based on the email address
     * <br>
     * For more information have a look at this tutorial:
     * https://www.baeldung.com/spring-security-authentication-with-a-database
     *
     * @param email the email address
     * @return a Spring Security user
     * @throws UsernameNotFoundException is thrown if the specified user does not exists
     */
    @Override
    UserDetails loadUserByUsername(String email) throws UsernameNotFoundException;

    /**
     * Log in a user.
     *
     * @param userLoginDto login credentials
     * @return the JWT, if successful
     * @throws org.springframework.security.authentication.BadCredentialsException if credentials are bad
     */
    String login(UserLoginDto userLoginDto);

    /**
     * Register a user.
     *
     * @param userRegisterDto login credentials
     * @return the JWT, if successful
     * @throws org.springframework.security.authentication.BadCredentialsException if user is already logged in and is trying to register again
     * @throws ConflictException                                                   if the email address is already in use
     */
    DetailedUserDto register(UserRegisterDto userRegisterDto) throws ConflictException, BadCredentialsException;

    /**
     * Find an application user based on the email address.
     *
     * @param id the users id
     * @return an application user
     * @throws org.springframework.security.authentication.BadCredentialsException if user who is not an admin tries to get another user by email or is not logged in
     */
    DetailedUserDto getUserById(UUID id) throws BadCredentialsException;

    /**
     * Delete an application user based on the email address.
     *
     * @param id the users id
     * @throws org.springframework.security.authentication.BadCredentialsException if user who is not an admin tries to delete another user by email or is not logged in
     */
    void deleteUserById(UUID id) throws BadCredentialsException;

    /**
     * Update a user.
     *
     * @param userUpdateDto user to update
     * @return the updated user
     * @throws org.springframework.security.authentication.BadCredentialsException if user who is not an admin tries to update another user or is not logged in
     * @throws ConflictException                                                   if the email address is already in use
     */
    DetailedUserDto updateUser(UserUpdateDto userUpdateDto) throws BadCredentialsException, ConflictException;


    /**
     * Get all users.
     *
     * @param locked if true, only locked users are returned
     * @return list of all users
     * @throws org.springframework.security.authentication.BadCredentialsException if user who is not an admin tries to get all users or is not logged in
     */
    List<DetailedUserDto> getAllUsers(boolean locked) throws BadCredentialsException;

    /**
     * Search for users with the given email address and locked status.
     *
     * @param username the username string to search for
     * @param locked   the locked status to filter for
     * @return list of users matching the search criteria
     */
    List<DetailedUserDto> searchUsersWithUsernameAndLocked(String username, boolean locked);

    /**
     * Find a user based on the id.
     *
     * @param id the id
     * @return a Spring Security user
     */
    DetailedUserDto loadUserById(UUID id);

    /**
     * Send a password reset link to the user.
     *
     * @param email the email address of the user
     * @return a message
     */
    String sendPasswordResetLink(String email);

    /**
     * Reset the password of the user.
     *
     * @param token       the token
     * @param newPassword the new password
     * @param email       the email address of the user
     * @return a message
     */
    String setNewPassword(String newPassword, String email, UUID token);


}
