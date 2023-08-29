package at.ac.tuwien.sepm.groupphase.backend.integrationtest;

import at.ac.tuwien.sepm.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepm.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.*;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper.EventMapper;
import at.ac.tuwien.sepm.groupphase.backend.entity.enums.EventType;
import at.ac.tuwien.sepm.groupphase.backend.repository.EventRepository;
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
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Sql(scripts = "classpath:sql/insertEventTestData.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class EventEndpointTest implements TestData {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private EventMapper eventMapper;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private JwtTokenizer jwtTokenizer;

    @Autowired
    private SecurityProperties securityProperties;

    @Test
    public void givenNothing_whenGetAll_thenListSizeTen() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .get(EVENT_BASE_URI).header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());

        EventsWithCountDto events = objectMapper.readValue(response.getContentAsString(), EventsWithCountDto.class);
        List<DetailedEventDto> detailedEventDtos = events.getEvents();
        assertNotNull(detailedEventDtos);
        assertEquals(10, detailedEventDtos.size());
    }

    @Test
    public void givenNothing_createEventWithValidEventShouldReturnSavedEventWithCreatedStatus() throws Exception {
        CreateEventDto createEventDto = CreateEventDto.builder()
            .title(TEST_EVENT_TITLE)
            .beginDate(TEST_EVENT_START_DATE)
            .endDate(TEST_EVENT_END_DATE)
            .type(TEST_EVENT_TYPE)
            .performances(new HashSet<>())
            .image("1984")
            .build();

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .post(EVENT_BASE_URI).contentType(MediaType.APPLICATION_JSON)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
                .content(objectMapper.writeValueAsString(createEventDto)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = result.getResponse();

        assertEquals(HttpStatus.CREATED.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());

        DetailedEventDto detailedEventDto = objectMapper.readValue(response.getContentAsString(), DetailedEventDto.class);

        assertNotNull(detailedEventDto);
        assertAll(
            () -> assertNotNull(detailedEventDto.getId()),
            () -> assertEquals(createEventDto.getTitle(), detailedEventDto.getTitle()),
            () -> assertEquals(createEventDto.getBeginDate(), detailedEventDto.getBeginDate()),
            () -> assertEquals(createEventDto.getEndDate(), detailedEventDto.getEndDate()),
            () -> assertEquals(createEventDto.getType(), detailedEventDto.getType()),
            () -> assertEquals(createEventDto.getPerformances().size(), detailedEventDto.getPerformances().size())
        );
    }

    @Test
    public void givenOneEvent_whenGetAll_thenListWithSizeTenAndMatchingProperties() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .get(EVENT_BASE_URI)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = result.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());

        EventsWithCountDto events = objectMapper.readValue(response.getContentAsString(), EventsWithCountDto.class);
        List<DetailedEventDto> detailedEventDtos = events.getEvents();
        assertNotNull(detailedEventDtos);
        assertEquals(10, detailedEventDtos.size());
        DetailedEventDto detailedEventDto = detailedEventDtos.get(0);
        assertAll(
            () -> assertEquals(-1L, detailedEventDto.getId()),
            () -> assertEquals("testConcert", detailedEventDto.getTitle()),
            () -> assertEquals(LocalDate.parse("2000-09-28"), detailedEventDto.getBeginDate()),
            () -> assertEquals(LocalDate.parse("2023-09-28"), detailedEventDto.getEndDate()),
            () -> assertEquals(EventType.CONCERT, detailedEventDto.getType()),
            () -> assertEquals(1, detailedEventDto.getPerformances().size())
        );
    }

    @Test
    public void givenNothing_whenCreateEventWithInvalidEvent_thenShouldReturn422() throws Exception {
        CreateEventDto createEventDto = CreateEventDto.builder()
            .title(TEST_EVENT_TITLE)
            .beginDate(TEST_EVENT_START_DATE)
            .endDate(LocalDate.of(2019, 1, 1))
            .type(TEST_EVENT_TYPE)
            .performances(new HashSet<>())
            .build();

        mockMvc.perform(MockMvcRequestBuilders
                .post(EVENT_BASE_URI).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createEventDto)))
            .andExpect(status().isUnprocessableEntity()).andDo(print());
    }

    @Test
    public void givenNothing_whenCreateEventWithInvalidPerformance_thenShouldReturn422() throws Exception {
        //create invalid createPerformanceDto
        SimpleLocationDto simpleLocationDto = new SimpleLocationDto(-1L, "testLocation", "testAddress", "testCity", "testCountry", "testStreet", "some Description");
        List<String> performers = Arrays.asList("testPerformer1", "testPerformer2");
        CreatePerformanceDto createPerformanceDto = new CreatePerformanceDto("testPerformance", simpleLocationDto,
            null, performers, LocalDateTime.of(2019, 1, 1, 1, 1),
            LocalDateTime.of(2018, 1, 1, 1, 1), BigDecimal.valueOf(100.00),
            null);

        CreateEventDto createEventDto = CreateEventDto.builder()
            .title(TEST_EVENT_TITLE)
            .beginDate(TEST_EVENT_START_DATE)
            .endDate(TEST_EVENT_END_DATE)
            .type(TEST_EVENT_TYPE)
            .performances(new HashSet<>(List.of(createPerformanceDto)))
            .build();

        mockMvc.perform(MockMvcRequestBuilders
                .post(EVENT_BASE_URI).contentType(MediaType.APPLICATION_JSON)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
                .content(objectMapper.writeValueAsString(createEventDto)))
            .andExpect(status().isUnprocessableEntity()).andDo(print());
    }

    @Test
    public void givenNothing_createEventWithInvalidEventShouldReturn422() throws Exception {
        CreateEventDto createEventDto = CreateEventDto.builder()
            .title("")
            .beginDate(TEST_EVENT_START_DATE)
            .endDate(TEST_EVENT_END_DATE)
            .type(TEST_EVENT_TYPE)
            .performances(new HashSet<>())
            .build();

        mockMvc.perform(MockMvcRequestBuilders
                .post(EVENT_BASE_URI).contentType(MediaType.APPLICATION_JSON)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
                .content(objectMapper.writeValueAsString(createEventDto)))
            .andExpect(status().isUnprocessableEntity()).andDo(print());
    }

    @Test
    public void givenOneEvent_whenGetEventByIdWithValidId_thenShouldReturnEventWithMatchingProperties() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .get(EVENT_BASE_URI + "/-2")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = result.getResponse();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());

        DetailedEventDto detailedEventDto = objectMapper.readValue(response.getContentAsString(), DetailedEventDto.class);

        assertNotNull(detailedEventDto);
        assertAll(
            () -> assertEquals(-2L, detailedEventDto.getId()),
            () -> assertEquals("testFestival", detailedEventDto.getTitle()),
            () -> assertEquals(LocalDate.parse("2023-06-25"), detailedEventDto.getBeginDate()),
            () -> assertEquals(LocalDate.parse("2023-06-29"), detailedEventDto.getEndDate()),
            () -> assertEquals(EventType.FESTIVAL, detailedEventDto.getType()),
            () -> assertEquals(1, detailedEventDto.getPerformances().size())
        );
    }

    @Test
    public void givenOneEvent_whenUpdateEventWithValidEvent_thenShouldReturnUpdatedEventWithMatchingProperties() throws Exception {
        UpdateEventDto updateDetailedDto = new UpdateEventDto(-3L, "MichaelsEvent", TEST_EVENT_TYPE, TEST_EVENT_START_DATE, TEST_EVENT_END_DATE, TEST_EVENT_IMAGE, new HashSet<>());

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .put(EVENT_BASE_URI + "/-3").contentType(MediaType.APPLICATION_JSON)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
                .content(objectMapper.writeValueAsString(updateDetailedDto)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = result.getResponse();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());

        DetailedEventDto detailedEventDto = objectMapper.readValue(response.getContentAsString(), DetailedEventDto.class);

        assertNotNull(detailedEventDto);
        assertAll(
            () -> assertEquals(-3L, detailedEventDto.getId()),
            () -> assertEquals(updateDetailedDto.getTitle(), detailedEventDto.getTitle()),
            () -> assertEquals(updateDetailedDto.getBeginDate(), detailedEventDto.getBeginDate()),
            () -> assertEquals(updateDetailedDto.getEndDate(), detailedEventDto.getEndDate()),
            () -> assertEquals(updateDetailedDto.getType(), detailedEventDto.getType()),
            () -> assertEquals(updateDetailedDto.getPerformances().size(), detailedEventDto.getPerformances().size())
        );
    }

    @Test
    public void givenOneEvent_whenUpdateEventWithInvalidEvent_thenShouldReturn422() throws Exception {
        SimpleEventDto simpleEventDto = new SimpleEventDto(-1L, "", TEST_EVENT_TYPE, TEST_EVENT_START_DATE, TEST_EVENT_END_DATE, TEST_EVENT_IMAGE);

        mockMvc.perform(MockMvcRequestBuilders
                .put(EVENT_BASE_URI + "/-1").contentType(MediaType.APPLICATION_JSON)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
                .content(objectMapper.writeValueAsString(simpleEventDto)))
            .andExpect(status().isUnprocessableEntity()).andDo(print());
    }

    @Test
    public void givenOneEvent_whenDeleteEventTwice_thenShouldReturnNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                .delete(EVENT_BASE_URI + "/-3").header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andExpect(status().isNoContent()).andDo(print());
        mockMvc.perform(MockMvcRequestBuilders
                .delete(EVENT_BASE_URI + "/-3").header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andExpect(status().isNotFound()).andDo(print());
    }

    @Test
    public void givenSearchDtoWithValidPerformanceParamsReturnsOnlyPerformanceElements() throws Exception {
        SearchDto searchDto = new SearchDto();
        searchDto.setEventName("testConcert2");
        searchDto.setPerformanceDate(LocalDate.parse("2023-05-15"));
        searchDto.setPerformanceTime(LocalTime.parse("12:00:00"));
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .get(EVENT_BASE_URI + "/search")
                .param("eventName", searchDto.getEventName())
                .param("performanceDate", searchDto.getPerformanceDate().toString())
                .param("performanceTime", searchDto.getPerformanceTime().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = result.getResponse();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());

        SearchResultsDto found = objectMapper.readValue(response.getContentAsString(), SearchResultsDto.class);
        List<DetailedEventDto> foundEvents = found.getEvents();
        List<DetailedPerformanceDto> foundPerformances = found.getPerformances();
        List<SimpleLocationDto> foundLocations = found.getLocations();
        List<String> foundArtists = found.getArtists();
        assertNull(foundEvents);
        assertEquals(1, foundPerformances.size());
        assertThat(foundPerformances)
            .map(DetailedPerformanceDto::getId, DetailedPerformanceDto::getTitle)
            .contains(Assertions.tuple(-3L, "testPerformance3"));
        assertNull(foundArtists);
        assertNull(foundLocations);
    }


}
