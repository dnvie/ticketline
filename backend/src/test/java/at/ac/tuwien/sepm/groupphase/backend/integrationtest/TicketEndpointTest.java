package at.ac.tuwien.sepm.groupphase.backend.integrationtest;

import at.ac.tuwien.sepm.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepm.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CreateTicketDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedTicketDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedTicketDtoWithOrder;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper.TicketMapper;
import at.ac.tuwien.sepm.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepm.groupphase.backend.repository.TicketRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepm.groupphase.backend.security.JwtTokenizer;
import at.ac.tuwien.sepm.groupphase.backend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Sql(scripts = "classpath:sql/insertEventTestData.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class TicketEndpointTest implements TestData {
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private TicketMapper ticketMapper;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private JwtTokenizer jwtTokenizer;

    @Autowired
    private SecurityProperties securityProperties;

    @Test
    public void findAllWithValidUserShouldReturnListWithSizeThree() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .get(TICKET_BASE_URI).header(securityProperties.getAuthHeader(),
                    jwtTokenizer.getAuthToken("ac55a452-f33d-42fe-9e85-185fc1f273ba", ADMIN_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());

        List<DetailedTicketDto> detailedTicketDtos = Arrays.asList(objectMapper.readValue(response.getContentAsString(), DetailedTicketDto[].class));
        assertNotNull(detailedTicketDtos);
        assertEquals(3, detailedTicketDtos.size());
        assertThat(detailedTicketDtos)
            .map(DetailedTicketDto::getId, DetailedTicketDto::getPrice)
            .contains(
                tuple(-1L, new BigDecimal("150.00")),
                tuple(-2L, new BigDecimal("120.00")),
                tuple(-3L, new BigDecimal("120.00"))
            );
    }

    @Test
    public void findAllWithInvalidUserShouldReturn401() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .get(TICKET_BASE_URI).header(securityProperties.getAuthHeader(),
                    jwtTokenizer.getAuthToken("1984a452-f33d-42fe-9e85-185fc1f273ba", USER_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    }

    @Test
    public void getTicketsForPerformanceShouldReturnListWithSizeTwo() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .get(TICKET_BASE_URI + "/performance/" + -2).header(securityProperties.getAuthHeader(),
                    jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());

        List<DetailedTicketDto> detailedTicketDtos = Arrays.asList(objectMapper.readValue(response.getContentAsString(), DetailedTicketDto[].class));
        assertNotNull(detailedTicketDtos);
        assertEquals(2, detailedTicketDtos.size());
        assertThat(detailedTicketDtos)
            .map(DetailedTicketDto::getId, DetailedTicketDto::getPrice)
            .contains(
                tuple(-2L, new BigDecimal("120.00")),
                tuple(-3L, new BigDecimal("120.00"))
            );
    }

    @Test
    public void getTicketsForPerformanceWithInvalidPerformanceShouldReturnEmptyList() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .get(TICKET_BASE_URI + "/performance/" + -1984).header(securityProperties.getAuthHeader(),
                    jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());

        List<DetailedTicketDtoWithOrder> detailedTicketDtos = Arrays.asList(objectMapper.readValue(response.getContentAsString(), DetailedTicketDtoWithOrder[].class));
        assertEquals(0, detailedTicketDtos.size());
    }

    @Test
    public void createTicketWithValidTicketShouldReturn201AndCreatedTicket() throws Exception {
        CreateTicketDto toCreate = new CreateTicketDto(-2L, UUID.fromString("b1c9473d-02a1-437f-aaaf-8828e0093716"), true, false);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .post(TICKET_BASE_URI).header(securityProperties.getAuthHeader(),
                    jwtTokenizer.getAuthToken("ac55a452-f33d-42fe-9e85-185fc1f273ba", ADMIN_ROLES))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(toCreate)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.CREATED.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());

        DetailedTicketDto createdTicket = objectMapper.readValue(response.getContentAsString(), DetailedTicketDto.class);
        assertNotNull(createdTicket);
        assertAll(
            () -> assertNotNull(createdTicket.getId()),
            () -> assertEquals(toCreate.getForPerformance(), createdTicket.getForPerformance().getId()),
            () -> assertEquals(toCreate.getForSeat(), createdTicket.getSeat().getId()),
            () -> assertEquals(toCreate.isReserved(), createdTicket.isReserved()),
            () -> assertEquals(new BigDecimal("150.00"), createdTicket.getPrice())
        );
    }

    @Test
    public void createTicketWithInvalidPerformanceIdShouldReturn409() throws Exception {
        CreateTicketDto toCreate = new CreateTicketDto(-1984L, UUID.fromString("b1c9473d-02a1-437f-aaaf-8828e0093716"), true, false);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .post(TICKET_BASE_URI).header(securityProperties.getAuthHeader(),
                    jwtTokenizer.getAuthToken("ac55a452-f33d-42fe-9e85-185fc1f273ba", ADMIN_ROLES))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(toCreate)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.CONFLICT.value(), response.getStatus());
    }

    @Test
    public void createTicketWithTakenSeatAndPerformanceShouldReturn409() throws Exception {
        CreateTicketDto toCreate = new CreateTicketDto(-1L, UUID.fromString("b1c9473d-02a1-437f-aaaf-8828e0093716"), true, false);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .post(TICKET_BASE_URI).header(securityProperties.getAuthHeader(),
                    jwtTokenizer.getAuthToken("90857b96-a69b-466b-90de-08c0ea4e66f4", ADMIN_ROLES))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(toCreate)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.CONFLICT.value(), response.getStatus());
    }

    @Test
    public void createTicketWithInvalidUserShouldReturn401() throws Exception {
        CreateTicketDto toCreate = new CreateTicketDto(-2L, UUID.fromString("b1c9473d-02a1-437f-aaaf-8828e0093716"), true, false);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .post(TICKET_BASE_URI).header(securityProperties.getAuthHeader(),
                    jwtTokenizer.getAuthToken("1984a452-f33d-42fe-9e85-185fc1f273ba", USER_ROLES))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(toCreate)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    }

    @Test
    public void deleteTicketWithValidIdShouldReturn204() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .delete(TICKET_BASE_URI + "/" + -1).header(securityProperties.getAuthHeader(),
                    jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatus());

        //Assert that ticket was deleted terminally
        assertFalse(ticketRepository.findById(-1L).isPresent());
    }

    @Test
    public void deleteTicketWithInvalidIdShouldReturn404() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .delete(TICKET_BASE_URI + "/" + -1984).header(securityProperties.getAuthHeader(),
                    jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
    }
}

