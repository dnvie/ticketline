package at.ac.tuwien.sepm.groupphase.backend.integrationtest;

import at.ac.tuwien.sepm.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepm.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedUserDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.SimpleMessageDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper.UserMapper;
import at.ac.tuwien.sepm.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepm.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepm.groupphase.backend.security.JwtTokenizer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Sql(scripts = "classpath:sql/deleteFromDB.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class UserEndpointTest implements TestData {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtTokenizer jwtTokenizer;

    @Autowired
    private SecurityProperties securityProperties;

    private ApplicationUser user = ApplicationUser.builder()
        .firstName("Test")
        .lastName("User")
        .email("test@email.com")
        .password("password")
        .admin(false)
        .enabled(true)
        .counter(0)
        .build();

    @BeforeEach
    public void beforeEach() {
        userRepository.deleteAll();
        user = ApplicationUser.builder()
            .firstName("Test")
            .lastName("User")
            .email("test@email.com")
            .password("password")
            .admin(false)
            .enabled(true)
            .counter(0)
            .build();
    }

    @Test
    public void givenNothing_whenFindAll_thenEmptyList() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(USER_BASE_URI + "?locked=false")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());

        List<SimpleMessageDto> simpleMessageDtos = Arrays.asList(objectMapper.readValue(response.getContentAsString(),
            SimpleMessageDto[].class));
        assertEquals(0, simpleMessageDtos.size());
    }

    @Test
    public void givenOneUser_whenFindAll_thenListWithSizeOneAndMessageWithPropertyEmail()
        throws Exception {

        userRepository.save(user);
        MvcResult mvcResult = this.mockMvc.perform(get(USER_BASE_URI + "?locked=false")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());

        List<DetailedUserDto> detailedUserDtos = Arrays.asList(objectMapper.readValue(response.getContentAsString(),
            DetailedUserDto[].class));

        assertEquals(1, detailedUserDtos.size());

        DetailedUserDto detailedUserDto = detailedUserDtos.get(0);
        assertAll(
            () -> assertEquals(user.getId(), detailedUserDto.getId()),
            () -> assertEquals(user.getEmail(), detailedUserDto.getEmail())
        );
    }

    @Test
    public void givenTwoUser_whenFindAllThatAreLocked_thenListWithSizeOneAndMessageWithPropertyEmail()
        throws Exception {
        ApplicationUser user_locked = user;
        userRepository.save(user);
        user_locked.setEnabled(false);
        userRepository.save(user_locked);
        MvcResult mvcResult = this.mockMvc.perform(get(USER_BASE_URI + "?locked=true")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());

        List<DetailedUserDto> detailedUserDtos = Arrays.asList(objectMapper.readValue(response.getContentAsString(),
            DetailedUserDto[].class));

        assertEquals(1, detailedUserDtos.size());

        DetailedUserDto detailedUserDto = detailedUserDtos.get(0);
        assertAll(
            () -> assertEquals(user.getId(), detailedUserDto.getId()),
            () -> assertEquals(user.getEmail(), detailedUserDto.getEmail())
        );
    }

    @Test
    public void givenOneUser_whenFindById_thenOneUserWithPropertyEmail()
        throws Exception {

        userRepository.save(user);
        MvcResult mvcResult = this.mockMvc.perform(get(USER_BASE_URI + "/" + user.getId())
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());

        DetailedUserDto detailedUserDtos = objectMapper.readValue(response.getContentAsString(),
            DetailedUserDto.class);


        DetailedUserDto detailedUserDto = detailedUserDtos;
        assertAll(
            () -> assertEquals(user.getId(), detailedUserDto.getId()),
            () -> assertEquals(user.getEmail(), detailedUserDto.getEmail())
        );
    }

    @Test
    public void NoUserGiven_whenRegister_thenListWithSizeOneAndUserWithPropertyEmail()
        throws Exception {

        MvcResult mvcResult = this.mockMvc.perform(post(USER_BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.CREATED.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());

        DetailedUserDto detailedUserDtos = objectMapper.readValue(response.getContentAsString(),
            DetailedUserDto.class);


        DetailedUserDto detailedUserDto = detailedUserDtos;

        assertEquals(user.getEmail(), detailedUserDto.getEmail());

        assertAll(
            () -> assertEquals(1, userRepository.findAll().size()),
            () -> assertEquals(user.getEmail(), userRepository.findAll().get(0).getEmail())
        );

    }


    @Test
    public void givenOneUser_whenUpdate_thenListWithSizeOneAndUserWithPropertyEmail()
        throws Exception {

        userRepository.save(user);
        user.setFirstName("Test2");
        MvcResult mvcResult = this.mockMvc.perform(put(USER_BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user))
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());

        DetailedUserDto detailedUserDtos = objectMapper.readValue(response.getContentAsString(),
            DetailedUserDto.class);

        DetailedUserDto detailedUserDto = detailedUserDtos;
        assertAll(
            () -> assertEquals(user.getId(), detailedUserDto.getId()),
            () -> assertEquals(user.getEmail(), detailedUserDto.getEmail()),
            () -> assertEquals(user.getFirstName(), detailedUserDto.getFirstName())
        );
    }

    @Test
    public void givenOneUser_whenDelete_thenListWithSizeZero()
        throws Exception {

        userRepository.save(user);
        MvcResult mvcResult = this.mockMvc.perform(delete(USER_BASE_URI + "/" + user.getId())
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());

        assertAll(
            () -> assertEquals(0, userRepository.findAll().size())
        );
    }

    @Test
    public void givenNothing_whenRegisterWithBadEmail_thenBadRequest()
        throws Exception {

        user.setEmail("bademail");
        MvcResult mvcResult = this.mockMvc.perform(post(USER_BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), response.getStatus());
    }

    @Test
    public void givenOneUser_whenUpdatePassword_thenOk()
        throws Exception {

        userRepository.save(user);
        user.setPassword("newPassword");
        MvcResult mvcResult = this.mockMvc.perform(post(USER_BASE_URI + "/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user))
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());
    }

    @Test
    public void givenOneUser_whenUpdatePasswordWithNotExistingUserEmail_thenOk()
        throws Exception {

        userRepository.save(user);
        user.setEmail("notexistingemail");
        user.setPassword("newPassword");
        MvcResult mvcResult = this.mockMvc.perform(post(USER_BASE_URI + "/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user))
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());
    }

    @Test
    public void givenOneUser_whenUpdatePasswordTwice_thenOk()
        throws Exception {

        userRepository.save(user);
        user.setPassword("newPassword");
        MvcResult mvcResult = this.mockMvc.perform(post(USER_BASE_URI + "/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user))
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());

        user.setPassword("newPassword2");
        MvcResult mvcResult2 = this.mockMvc.perform(post(USER_BASE_URI + "/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user))
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response2 = mvcResult2.getResponse();

        assertEquals(HttpStatus.OK.value(), response2.getStatus());
    }

    @Test
    public void givenTwoUser_whenSearchUsersByUserNameAndLockedFalse_thenListWithSizeTwo()
        throws Exception {

        userRepository.save(user);
        ApplicationUser user2 = new ApplicationUser();
        user2.setEmail("email2@mail.com");
        user2.setFirstName("Test2");
        user2.setLastName("Test2");
        user2.setPassword("password");
        user2.setEnabled(false);
        user2.setAdmin(false);
        user2.setCounter(0);
        userRepository.save(user2);

        MvcResult mvcResult = this.mockMvc.perform(get(USER_BASE_URI + "/search")
                .param("username", "Test")
                .param("locked", "false")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());


        List<DetailedUserDto> detailedUserDtos = Arrays.asList(objectMapper.readValue(response.getContentAsString(),
            DetailedUserDto[].class));

        assertAll(
            () -> assertEquals(2, detailedUserDtos.size()),
            () -> assertEquals(user.getEmail(), detailedUserDtos.get(0).getEmail()),
            () -> assertEquals(user2.getEmail(), detailedUserDtos.get(1).getEmail())
        );
    }

    @Test
    public void givenTwoUser_whenSearchUsersByUserNameAndLockedTrue_thenListWithSizeOne()
        throws Exception {

        userRepository.save(user);
        ApplicationUser user2 = new ApplicationUser();
        user2.setEmail("email2@mail.com");
        user2.setFirstName("Test2");
        user2.setLastName("Test2");
        user2.setPassword("password");
        user2.setEnabled(false);
        user2.setAdmin(false);
        user2.setCounter(0);
        userRepository.save(user2);

        MvcResult mvcResult = this.mockMvc.perform(get(USER_BASE_URI + "/search")
                .param("username", "Test")
                .param("locked", "true")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());

        List<DetailedUserDto> detailedUserDtos = Arrays.asList(objectMapper.readValue(response.getContentAsString(),
            DetailedUserDto[].class));

        assertAll(
            () -> assertEquals(1, detailedUserDtos.size()),
            () -> assertEquals(user2.getEmail(), detailedUserDtos.get(0).getEmail())
        );
    }
}
