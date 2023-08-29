package at.ac.tuwien.sepm.groupphase.backend.integrationtest;

import at.ac.tuwien.sepm.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepm.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.*;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper.PerformanceMapper;
import at.ac.tuwien.sepm.groupphase.backend.entity.enums.EventType;
import at.ac.tuwien.sepm.groupphase.backend.repository.PerformanceRepository;
import at.ac.tuwien.sepm.groupphase.backend.security.JwtTokenizer;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//TODO: refactor because of changes with seatmap
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Sql(scripts = "classpath:sql/insertEventTestData.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class PerformanceEndpointTest implements TestData {
    private final String PERFORMANCE_BASE_URI = "/api/v1/performance";
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private PerformanceMapper performanceMapper;

    @Autowired
    private PerformanceRepository performanceRepository;

    @Autowired
    private JwtTokenizer jwtTokenizer;

    @Autowired
    private SecurityProperties securityProperties;

    @Test
    public void findAllPerformancesReturnsListWithSizeThree() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .get(PERFORMANCE_BASE_URI).header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());
        List<DetailedPerformanceDto> detailedPerformanceDtos = Arrays.asList(objectMapper.readValue(response.getContentAsString(), DetailedPerformanceDto[].class));
        assertEquals(3, detailedPerformanceDtos.size());
        assertThat(detailedPerformanceDtos)
            .map(DetailedPerformanceDto::getId, DetailedPerformanceDto::getTitle)
            .contains(Assertions.tuple(-1L, "testPerformance1"))
            .contains(Assertions.tuple(-2L, "testPerformance2"))
            .contains(Assertions.tuple(-3L, "testPerformance3"));
    }

    @Test
    public void getPerformanceByIdReturnsNotFoundIfNoPerformanceIsFound() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .get(PERFORMANCE_BASE_URI + "/100000").header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
    }

    @Test
    public void getPerformanceByIdShouldReturnNotFoundWhenPerformanceIsDeletedBefore() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                .delete(PERFORMANCE_BASE_URI + "/-3").header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andExpect(status().isNoContent()).andDo(print());
        mockMvc.perform(MockMvcRequestBuilders
                .get(PERFORMANCE_BASE_URI + "/-3").header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andExpect(status().isNotFound()).andDo(print());
    }

    @Test
    public void createPerformanceWithValidDataShouldReturnTheNewlyCreatedPerformance() throws Exception {
        SimpleLocationDto locationDto = new SimpleLocationDto(-1L, "Ernst-Happel-Stadion", "Austria", "Vienna", "1020", "Prater1", "Football Stadium with Capacity of app. 50k");
        SimpleEventDto eventDto = new SimpleEventDto(-1L, "testConcert", EventType.CONCERT, LocalDate.parse("2000-09-28"), LocalDate.parse("2023-09-28"), TEST_EVENT_IMAGE);
        List<String> performers = new ArrayList<>();
        performers.add("p1");
        performers.add("p2");
        performers.add("p3");
        CreatePerformanceDto toCreate = new CreatePerformanceDto("created", locationDto, eventDto, performers, LocalDateTime.parse("2022-05-22T23:00:00"), LocalDateTime.parse("2023-05-22T23:00:00"), BigDecimal.valueOf(100), null);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .post(PERFORMANCE_BASE_URI).contentType(MediaType.APPLICATION_JSON)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
                .content(objectMapper.writeValueAsString(toCreate)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = result.getResponse();
        assertEquals(HttpStatus.CREATED.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());
        DetailedPerformanceDto created = objectMapper.readValue(response.getContentAsString(), DetailedPerformanceDto.class);
        assertNotNull(created);
        assertAll(
            () -> assertNotNull(created.getId()),
            () -> assertEquals("created", created.getTitle()),
            () -> assertEquals(created.getStartTime(), LocalDateTime.parse("2022-05-22T23:00:00")),
            () -> assertEquals(created.getEndTime(), LocalDateTime.parse("2023-05-22T23:00:00")),
            () -> assertEquals(created.getEvent().getId(), -1L),
            () -> assertEquals(created.getLocation().getId(), -1L),
            () -> assertEquals(created.getPerformers(), performers)
        );
    }

    @Test
    public void createPerformanceWithInvalidDataShouldReturn422() throws Exception {
        SimpleLocationDto locationDto = new SimpleLocationDto(-1L, "Ernst-Happel-Stadion", "Austria", "Vienna", "1020", "Prater1", "Football Stadium with Capacity of app. 50k");
        SimpleEventDto eventDto = new SimpleEventDto(-1L, "testConcert", EventType.CONCERT, LocalDate.parse("2000-09-28"), LocalDate.parse("2023-09-28"), TEST_EVENT_IMAGE);
        List<String> performers = new ArrayList<>();
        performers.add("p1");
        performers.add("p2");
        performers.add("");
        CreatePerformanceDto toCreate = new CreatePerformanceDto("created", locationDto, eventDto, performers, LocalDateTime.parse("2022-05-22T23:00:00"), LocalDateTime.parse("2023-05-22T23:00:00"), BigDecimal.valueOf(100), null);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .post(PERFORMANCE_BASE_URI).contentType(MediaType.APPLICATION_JSON)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
                .content(objectMapper.writeValueAsString(toCreate)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = result.getResponse();
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), response.getStatus());
    }

    @Test
    public void createPerformanceWithInvalidLocationIdShouldReturn409() throws Exception {
        SimpleLocationDto locationDto = new SimpleLocationDto(111L, "Ernst-Happel-Stadion", "Austria", "Vienna", "1020", "Prater1", "Football Stadium with Capacity of app. 50k");
        SimpleEventDto eventDto = new SimpleEventDto(-1L, "testConcert", EventType.CONCERT, LocalDate.parse("2000-09-28"), LocalDate.parse("2023-09-28"), TEST_EVENT_IMAGE);
        List<String> performers = new ArrayList<>();
        performers.add("p1");
        performers.add("p2");
        performers.add("p3");
        CreatePerformanceDto toCreate = new CreatePerformanceDto("created", locationDto, eventDto, performers, LocalDateTime.parse("2022-05-22T23:00:00"), LocalDateTime.parse("2023-05-22T23:00:00"), BigDecimal.valueOf(100), null);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .post(PERFORMANCE_BASE_URI).contentType(MediaType.APPLICATION_JSON)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
                .content(objectMapper.writeValueAsString(toCreate)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = result.getResponse();
        assertEquals(HttpStatus.CONFLICT.value(), response.getStatus());
    }

    @Test
    public void whenUpdatePerformanceWithValidDataGetPerformanceByIdShouldReturnUpdatedPerformance() throws Exception {
        SimpleLocationDto locationDto = new SimpleLocationDto(-1L, "Ernst-Happel-Stadion", "Austria", "Vienna", "1020", "Prater1", "Football Stadium with Capacity of app. 50k");
        SimpleEventDto eventDto = new SimpleEventDto(-1L, "testConcert", EventType.CONCERT, LocalDate.parse("2000-09-28"), LocalDate.parse("2023-09-28"), TEST_EVENT_IMAGE);
        List<String> performers = new ArrayList<>();
        performers.add("p1");
        performers.add("p2");
        performers.add("p3");
        DetailedPerformanceDto toUpdate =
            new DetailedPerformanceDto(-1L, "anotherName", locationDto, eventDto, performers, LocalDateTime.parse("2022-05-22T23:00:00"), LocalDateTime.parse("2023-05-20T23:00:00"), BigDecimal.valueOf(100),
                UUID.fromString("24e55098-9e8b-4d7a-b573-5dcd752a8097"));
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .put(PERFORMANCE_BASE_URI + "/-1").contentType(MediaType.APPLICATION_JSON)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
                .content(objectMapper.writeValueAsString(toUpdate)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = result.getResponse();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .get(PERFORMANCE_BASE_URI + "/-1").header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse responseFind = mvcResult.getResponse();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        DetailedPerformanceDto found = objectMapper.readValue(responseFind.getContentAsString(), DetailedPerformanceDto.class);
        assertNotNull(found);
        assertAll(
            () -> assertNotNull(found.getId()),
            () -> assertEquals("anotherName", found.getTitle()),
            () -> assertEquals(found.getStartTime(), LocalDateTime.parse("2022-05-22T23:00:00")),
            () -> assertEquals(found.getEndTime(), LocalDateTime.parse("2023-05-20T23:00:00")),
            () -> assertEquals(found.getEvent().getId(), -1L),
            () -> assertEquals(found.getLocation().getId(), -1L),
            () -> assertEquals(found.getPerformers(), performers)
        );
    }
}
