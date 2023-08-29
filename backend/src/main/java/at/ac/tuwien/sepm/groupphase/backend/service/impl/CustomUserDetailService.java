package at.ac.tuwien.sepm.groupphase.backend.service.impl;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedUserDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.UserLoginDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.UserRegisterDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.UserUpdateDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper.UserMapper;
import at.ac.tuwien.sepm.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepm.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepm.groupphase.backend.security.JwtTokenizer;
import at.ac.tuwien.sepm.groupphase.backend.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CustomUserDetailService implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenizer jwtTokenizer;

    private final JavaMailSender mailSender;

    private final UserMapper userMapper;

    // create a map for token and email + timestamp
    private Map<String, String> tokenMap = new HashMap<>();


    @Autowired
    public CustomUserDetailService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenizer jwtTokenizer, UserMapper userMapper,
                                   JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenizer = jwtTokenizer;
        this.userMapper = userMapper;
        this.mailSender = mailSender;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        LOGGER.debug("Load all user by email");
        try {
            ApplicationUser applicationUser = findApplicationUserByEmail(email);

            List<GrantedAuthority> grantedAuthorities;
            if (Boolean.TRUE.equals(applicationUser.getAdmin())) {
                grantedAuthorities = AuthorityUtils.createAuthorityList("ROLE_ADMIN", "ROLE_USER");
            } else {
                grantedAuthorities = AuthorityUtils.createAuthorityList("ROLE_USER");
            }

            return new User(applicationUser.getId().toString(), applicationUser.getPassword(), grantedAuthorities);
        } catch (NotFoundException e) {
            throw new UsernameNotFoundException(e.getMessage(), e);
        }
    }

    @Override
    public String login(UserLoginDto userLoginDto) {
        LOGGER.debug("Login user");
        UserDetails userDetails;
        ApplicationUser applicationUser;
        try {
            userDetails = loadUserByUsername(userLoginDto.getEmail());
            applicationUser = findApplicationUserByEmail(userLoginDto.getEmail());
        } catch (UsernameNotFoundException | NotFoundException | BadCredentialsException e) {
            throw new BadCredentialsException("Username or password is incorrect");
        }

        if (Boolean.TRUE.equals(applicationUser.getEnabled())) {
            if (userDetails != null
                && !passwordEncoder.matches(userLoginDto.getPassword(), userDetails.getPassword())
            ) {
                boolean disabled = !addFailedLoginAttemptAndDisableIfNecessary(applicationUser);
                throw new BadCredentialsException("Username or password is incorrect" + (disabled ? "! Account is now disabled!" : ""));
            }
            if (userDetails != null
                && userDetails.isAccountNonExpired()
                && userDetails.isAccountNonLocked()
                && userDetails.isCredentialsNonExpired()
            ) {
                List<String> roles = userDetails.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();
                return jwtTokenizer.getAuthToken(userDetails.getUsername(), roles);
            }
            throw new BadCredentialsException("Session is expired or account is locked");
        } else {
            throw new BadCredentialsException("Account is disabled");
        }
    }

    @Override
    @Transactional
    public DetailedUserDto register(UserRegisterDto userRegisterDto) throws BadCredentialsException, ConflictException {
        LOGGER.debug("Register user");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = false;
        if (auth != null) {
            if (auth.getAuthorities().stream().allMatch(aut -> aut.getAuthority().equals("ROLE_USER"))) {
                throw new BadCredentialsException("You must be Admin to register new Users");
            }
            Boolean a = userRegisterDto.getAdmin();
            isAdmin = a != null && a
                && auth.getAuthorities().stream().anyMatch(aut -> aut.getAuthority().equals("ROLE_ADMIN"));
        }
        userRegisterDto.setAdmin(isAdmin);

        // check if user already exists
        if (!userRepository.findUserByEmail(userRegisterDto.getEmail()).isEmpty()) {
            List<String> errors = new ArrayList<>();
            errors.add("User with this email already exists");
            throw new ConflictException("Conflicts occurred!", errors);
        }

        //encode password
        userRegisterDto.setPassword(passwordEncoder.encode(userRegisterDto.getPassword()));

        // create user
        ApplicationUser applicationUser = userMapper.userRegisterDtoToApplicationUser(userRegisterDto);

        // save user
        userRepository.save(applicationUser);

        DetailedUserDto user = userMapper.applicationUserToDetailedUserDto(applicationUser);
        user.setId(findApplicationUserByEmail(userRegisterDto.getEmail()).getId());
        return user;

    }

    @Override
    public DetailedUserDto getUserById(UUID id) throws BadCredentialsException {
        LOGGER.debug("Get user by email");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new BadCredentialsException("You must be logged in to get user information");
        } else if (
            auth.getAuthorities().stream().allMatch(aut -> aut.getAuthority().equals("ROLE_USER"))
                && !auth.getName().equals(id.toString())
        ) {
            throw new BadCredentialsException("You must be Admin to get another user's information");
        }
        return userMapper.applicationUserToDetailedUserDto(findApplicationUserById(id));
    }

    @Transactional
    @Override
    public void deleteUserById(UUID id) throws BadCredentialsException {
        LOGGER.debug("Delete user by email");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new BadCredentialsException("You must be logged in to delete a user");
        } else if (
            auth.getAuthorities().stream().allMatch(aut -> aut.getAuthority().equals("ROLE_USER"))
                && !auth.getName().equals(id.toString())
        ) {
            throw new BadCredentialsException("You must be Admin to delete another user");
        }
        long deleted = userRepository.deleteByUuid(id);

        if (deleted > 1) {
            throw new IllegalStateException("Multiple users with the same id");
        }
        if (deleted == 0) {
            throw new NotFoundException(String.format("Could not find the user with the id %s", id.toString()));
        }
    }

    @Override
    public List<DetailedUserDto> getAllUsers(boolean locked) throws BadCredentialsException {
        LOGGER.debug("Get all users");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new BadCredentialsException("You must be logged in to get all users");
        } else if (
            auth.getAuthorities().stream().allMatch(aut -> aut.getAuthority().equals("ROLE_USER"))
        ) {
            throw new BadCredentialsException("You must be Admin to get all users");
        }
        List<ApplicationUser> allUsers = !locked ? userRepository.findAll() : userRepository.findAllByEnabledIsFalse();
        return userMapper.applicationUserToDetailedUserDto(allUsers);
    }

    @Override
    public List<DetailedUserDto> searchUsersWithUsernameAndLocked(String username, boolean locked) {
        LOGGER.debug("Search users with email and locked");
        //remove all whitespaces in front and back of the string
        username = username.strip();
        // if locked is false we want to find all user
        List<ApplicationUser> allUsers = locked ? userRepository.findApplicationUserByEnabledIsFalseAndFirstNameContainingIgnoreCaseOrLastNameIsContainingIgnoreCase(username, username)
            : userRepository.findApplicationUserByFirstNameContainingIgnoreCaseOrLastNameIsContainingIgnoreCase(username, username);
        return userMapper.applicationUserToDetailedUserDto(allUsers);
    }

    @Override
    public DetailedUserDto loadUserById(UUID id) {
        LOGGER.debug("Load user by id");
        ApplicationUser user;
        try {
            user = findApplicationUserById(id);
        } catch (NotFoundException e) {
            return null;
        }
        return userMapper.applicationUserToDetailedUserDto(user);
    }


    @Override
    public DetailedUserDto updateUser(UserUpdateDto userUpdateDto) throws BadCredentialsException, ConflictException {
        LOGGER.debug("Update User");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new BadCredentialsException("You must be logged in to update a user");
        } else if (
            auth.getAuthorities().stream().allMatch(aut -> aut.getAuthority().equals("ROLE_USER"))
                && !auth.getName().equals(userUpdateDto.getId().toString())
        ) {
            throw new BadCredentialsException("You must be Admin to update another user");
        }

        //check if user with this id exists if not throw exception
        if (userRepository.findUserById(userUpdateDto.getId()).isEmpty()) {
            throw new NotFoundException(String.format("Could not find the user with the id %s", userUpdateDto.getId().toString()));
        }

        //check if user with this id exists if not throw exception
        if (userRepository.findUserById(userUpdateDto.getId()).isEmpty()) {
            throw new NotFoundException(String.format("Could not find the user with the id %s", userUpdateDto.getId().toString()));
        }

        setPropertiesForUpdate(userUpdateDto, auth);


        // create user
        ApplicationUser applicationUser = userMapper.userUpdateDtoToApplicationUser(userUpdateDto);

        // save user
        userRepository.save(applicationUser);

        return userMapper.applicationUserToDetailedUserDto(applicationUser);
    }

    @Override
    public String sendPasswordResetLink(String email) {
        // use java mail to send email
        LOGGER.debug("Send password reset link");
        try {
            // get the user by email. the email format is {"email":"<email>"}
            String userEmail = email.substring(10, email.length() - 2);
            ApplicationUser applicationUser = findApplicationUserByEmail(userEmail);

            //check if user already has a token
            if (tokenMap.containsKey(applicationUser.getEmail())) {

                String token = tokenMap.get(applicationUser.getEmail()).split(",")[0];
                String time = tokenMap.get(applicationUser.getEmail()).split(",")[1];
                long currentTime = System.currentTimeMillis();
                long timeDifference = currentTime - Long.parseLong(time);
                if (timeDifference < 600000) {
                    return "You already requested a password reset link. Please check your email.";
                } else {
                    tokenMap.remove(applicationUser.getEmail());
                }
            }

            String token = UUID.randomUUID().toString();

            tokenMap.put(applicationUser.getEmail(), token + "," + System.currentTimeMillis());

            String emailtext = new String(Files.readAllBytes(Paths.get("src/main/resources/email-html-text")));

            emailtext = emailtext.replace("$Name", applicationUser.getFirstName());
            emailtext = emailtext.replace("$Link", "http://localhost:4200/#/password-reset/" + token);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setText(emailtext, true); // Use this or above line.
            helper.setTo(applicationUser.getEmail());
            helper.setSubject("Ticketline Password Reset Request");
            helper.setFrom("noreply@ticketline.at");
            mailSender.send(mimeMessage);


        } catch (NotFoundException e) {
            return "Email Sent";
        } catch (MessagingException | IOException e) {
            throw new RuntimeException(e);
        }

        return "Email Sent";
    }

    @Override
    public String setNewPassword(String newPassword, String email, UUID token) {
        LOGGER.debug("Set new password");
        try {
            // get the user by email. the email format is {"email":"<email>"}
            ApplicationUser applicationUser = findApplicationUserByEmail(email);

            if (!tokenMap.containsKey(applicationUser.getEmail())) {
                return "Email not found";
            }
            String tokenString = tokenMap.get(applicationUser.getEmail()).split(",")[0];
            if (!tokenString.equals(token.toString())) {
                return "Token not found";
            }

            // check if the time difference is less than 2 hours
            String time = tokenMap.get(applicationUser.getEmail()).split(",")[1];
            long currentTime = System.currentTimeMillis();
            long timeDifference = currentTime - Long.parseLong(time);
            if (timeDifference > 7200000) {
                return "Token expired";
            }

            tokenMap.remove(applicationUser.getEmail());
            // update the password
            applicationUser.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(applicationUser);

            return "Password changed";
        } catch (NotFoundException e) {
            return "Email not found";
        }
    }

    private ApplicationUser findApplicationUserByEmail(String email) {
        LOGGER.debug("Find application user by email");
        ApplicationUser applicationUser = userRepository.findUserByEmail(email).stream().reduce((a, b) -> {
            throw new IllegalStateException("Multiple elements: " + a + ", " + b);
        }).orElse(null);

        if (applicationUser != null) {
            return applicationUser;
        }
        throw new NotFoundException(String.format("Could not find the user with the email address %s", email));
    }

    private ApplicationUser findApplicationUserById(UUID id) {
        LOGGER.debug("Find application user by id");
        ApplicationUser applicationUser = userRepository.findUserById(id).stream().reduce((a, b) -> {
            throw new IllegalStateException("Multiple elements: " + a + ", " + b);
        }).orElse(null);

        if (applicationUser != null) {
            return applicationUser;
        }
        throw new NotFoundException(String.format("Could not find the user with the id %s", id.toString()));
    }

    private boolean addFailedLoginAttemptAndDisableIfNecessary(ApplicationUser applicationUser) {
        LOGGER.debug("Add failed login attempt and disable if necessary");
        applicationUser.setCounter(applicationUser.getCounter() + 1);
        if (applicationUser.getCounter() >= 5) {
            applicationUser.setEnabled(false);
        }
        userRepository.save(applicationUser);
        return applicationUser.getEnabled();
    }

    private void setPropertiesForUpdate(UserUpdateDto userUpdateDto, Authentication auth) throws ConflictException {
        boolean roleIsAdmin = auth.getAuthorities().stream().anyMatch(aut -> aut.getAuthority().equals("ROLE_ADMIN"));

        Boolean a = userUpdateDto.getAdmin();
        userUpdateDto.setAdmin(a != null
            && a
            && roleIsAdmin);

        boolean emailIsUpdated =
            userRepository.findUserById(userUpdateDto.getId()).stream().map(ApplicationUser::getEmail).noneMatch(userUpdateDto.getEmail()::equals);

        if (
            emailIsUpdated
                && !userRepository.findUserByEmail(userUpdateDto.getEmail()).isEmpty()
        ) {
            List<String> errors = new ArrayList<>();
            errors.add("User with this email already exists");
            throw new ConflictException("Conflicts occurred!", errors);
        }

        if (userUpdateDto.getPassword() != null) {
            userUpdateDto.setPassword(passwordEncoder.encode(userUpdateDto.getPassword()));
        } else {
            userUpdateDto.setPassword(
                userRepository.findUserById(userUpdateDto.getId()).stream().map(ApplicationUser::getPassword).findFirst().orElseThrow()
            );
        }

        boolean wasEnabled = userRepository.findUserById(userUpdateDto.getId()).stream().map(ApplicationUser::getEnabled).findFirst().orElse(false);

        if (roleIsAdmin) {
            userUpdateDto.setEnabled(userUpdateDto.getEnabled() != null ? userUpdateDto.getEnabled() : wasEnabled);
        } else {
            if (userUpdateDto.getEnabled() == null) {
                userUpdateDto.setEnabled(wasEnabled);
            } else if (wasEnabled != Boolean.TRUE.equals(userUpdateDto.getEnabled())) {
                throw new BadCredentialsException("You must be admin to enable/disable a user");
            }
        }
    }

}
