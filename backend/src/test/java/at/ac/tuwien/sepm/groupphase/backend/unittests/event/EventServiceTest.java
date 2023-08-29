package at.ac.tuwien.sepm.groupphase.backend.unittests.event;

import at.ac.tuwien.sepm.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.*;
import at.ac.tuwien.sepm.groupphase.backend.entity.enums.EventType;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepm.groupphase.backend.service.EventService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "classpath:sql/insertEventTestData.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class EventServiceTest implements TestData {
    @Autowired
    private EventService eventService;

    @Test
    public void givenNothing_whenFindAll_thenFindListWithAllEvents() {
        Pageable pageable = Pageable.ofSize(12);
        List<DetailedEventDto> events = eventService.findAll(pageable);

        assertEquals(10, events.size());
        assertThat(events)
            .map(DetailedEventDto::getId, DetailedEventDto::getTitle)
            .contains(org.assertj.core.api.Assertions.tuple(-1L, "testConcert"))
            .contains(org.assertj.core.api.Assertions.tuple(-2L, "testFestival"))
            .contains(org.assertj.core.api.Assertions.tuple(-3L, "testTheater"))
            .contains(org.assertj.core.api.Assertions.tuple(-4L, "testOpera"))
            .contains(org.assertj.core.api.Assertions.tuple(-5L, "testMusical"))
            .contains(org.assertj.core.api.Assertions.tuple(-6L, "testMovie"))
            .contains(org.assertj.core.api.Assertions.tuple(-7L, "testOther"))
            .contains(org.assertj.core.api.Assertions.tuple(-8L, "testOther"))
            .contains(org.assertj.core.api.Assertions.tuple(-9L, "testFestival2"))
            .contains(org.assertj.core.api.Assertions.tuple(-10L, "testConcert2"));
    }

    @Test
    public void createEventWithValidEventShouldReturnDetailedCreatedEvent() throws Exception {
        CreateEventDto createEventDto = new CreateEventDto("testTitle", EventType.MUSICAL,
            LocalDate.of(2021, 6, 1), LocalDate.of(2021, 6, 2), TEST_EVENT_IMAGE, new HashSet<>());
        DetailedEventDto detailedEventDto = eventService.createEvent(createEventDto);

        assertAll(
            () -> assertEquals(createEventDto.getTitle(), detailedEventDto.getTitle()),
            () -> assertEquals(createEventDto.getType(), detailedEventDto.getType()),
            () -> assertEquals(createEventDto.getBeginDate(), detailedEventDto.getBeginDate()),
            () -> assertEquals(createEventDto.getEndDate(), detailedEventDto.getEndDate()),
            () -> assertEquals(createEventDto.getPerformances().size(), detailedEventDto.getPerformances().size())
        );
    }

    @Test
    public void createEventWithInvalidEventShouldThrowValidationException() {
        CreateEventDto createEventDto = new CreateEventDto(null, null, null, null, null, null);
        ValidationException exception = assertThrows(ValidationException.class, () -> eventService.createEvent(createEventDto));
        //Check for correct messages inside exception
        assertAll(
            () -> assertTrue(exception.getMessage().contains("Title must not be null")),
            () -> assertTrue(exception.getMessage().contains("Type must not be null")),
            () -> assertTrue(exception.getMessage().contains("Begin date must not be null")),
            () -> assertTrue(exception.getMessage().contains("End date must not be null"))
        );
    }

    @Test
    public void deleteEventWithValidIdShouldDeleteEvent() throws Exception {
        eventService.deleteEvent(-3L);
        Pageable pageable = Pageable.ofSize(12);
        List<DetailedEventDto> events = eventService.findAll(pageable);
        assertEquals(9, events.size());
        assertThat(events)
            .map(DetailedEventDto::getId, DetailedEventDto::getTitle)
            .contains(tuple(-2L, "testFestival"))
            .contains(tuple(-1L, "testConcert"))
            .contains(tuple(-4L, "testOpera"))
            .contains(tuple(-5L, "testMusical"))
            .contains(tuple(-6L, "testMovie"))
            .contains(tuple(-7L, "testOther"))
            .contains(tuple(-8L, "testOther"))
            .contains(tuple(-9L, "testFestival2"))
            .contains(tuple(-10L, "testConcert2"));
    }

    @Test
    public void deleteEventWithInvalidIdShouldThrowNotFoundException() {
        NotFoundException exception = assertThrows(NotFoundException.class, () -> eventService.deleteEvent(-330L));
        assertTrue(exception.getMessage().contains("Could not find event with id -330"));
    }

    @Test
    public void updateEventWithValidEventShouldReturnDetailedUpdatedEvent() throws Exception {
        Set<SimplePerformanceDto> performances = new HashSet<>();
        List<String> performers = new ArrayList<>();
        performers.add("testPerformer1");
        SimpleLocationDto locationDto = new SimpleLocationDto(-1L, "Ernst-Happel-Stadion", "Austria", "Vienna", "1020", "Prater1", "testCountry");
        performances.add(new SimplePerformanceDto(-1L, "testPerformance1", performers, locationDto, LocalDateTime.of(2022, 5, 20, 12, 0, 0),
            LocalDateTime.of(2023, 5, 20, 12, 0, 0), BigDecimal.valueOf(100), null));
        UpdateEventDto updateEventDto = new UpdateEventDto(-1L, "testTitle", EventType.MUSICAL,
            LocalDate.of(2021, 6, 1), LocalDate.of(2021, 6, 2), TEST_EVENT_IMAGE, performances);
        DetailedEventDto detailedEventDto = eventService.updateEvent(-1L, updateEventDto);
        assertNotNull(detailedEventDto);
        assertAll(
            () -> assertEquals(updateEventDto.getTitle(), detailedEventDto.getTitle()),
            () -> assertEquals(updateEventDto.getType(), detailedEventDto.getType()),
            () -> assertEquals(updateEventDto.getBeginDate(), detailedEventDto.getBeginDate()),
            () -> assertEquals(updateEventDto.getEndDate(), detailedEventDto.getEndDate()),
            () -> assertEquals(updateEventDto.getPerformances().size(), detailedEventDto.getPerformances().size())
        );
    }

    @Test
    public void updateEventWithInvalidEventShouldThrowValidationException() {
        UpdateEventDto updateEventDto = new UpdateEventDto(-1L, null, null, null, null, null, null);
        ValidationException exception = assertThrows(ValidationException.class, () -> eventService.updateEvent(-1L, updateEventDto));
        //Check for correct messages inside exception
        assertAll(
            () -> assertTrue(exception.getMessage().contains("Title must not be null")),
            () -> assertTrue(exception.getMessage().contains("Type must not be null")),
            () -> assertTrue(exception.getMessage().contains("Begin date must not be null")),
            () -> assertTrue(exception.getMessage().contains("End date must not be null"))
        );
    }

    @Test
    public void findOneWithValidIdShouldReturnDetailedEvent() {
        DetailedEventDto detailedEventDto = eventService.findOne(-1L);
        assertNotNull(detailedEventDto);
        assertAll(
            () -> assertEquals(-1L, detailedEventDto.getId()),
            () -> assertEquals("testConcert", detailedEventDto.getTitle()),
            () -> assertEquals(EventType.CONCERT, detailedEventDto.getType()),
            () -> assertEquals(LocalDate.of(2000, 9, 28), detailedEventDto.getBeginDate()),
            () -> assertEquals(LocalDate.of(2023, 9, 28), detailedEventDto.getEndDate()),
            () -> assertEquals(1, detailedEventDto.getPerformances().size())
        );
    }

    @Test
    public void findOneWithInvalidIdShouldThrowNotFoundException() {
        NotFoundException exception = assertThrows(NotFoundException.class, () -> eventService.findOne(-330L));
        assertTrue(exception.getMessage().contains("Could not find event with id -330"));
    }

    @Test
    public void searchWithOnlySearchbarGivenInParametersShouldReturnAllMatchingEntities() {
        SearchDto params = new SearchDto();
        params.setSearchBar("2");
        SearchResultsDto found = eventService.search(params);
        List<DetailedEventDto> foundEvents = found.getEvents();
        List<DetailedPerformanceDto> foundPerformances = found.getPerformances();
        List<SimpleLocationDto> foundLocations = found.getLocations();
        List<String> foundArtists = found.getArtists();
        assertEquals(2, foundEvents.size());
        assertThat(foundEvents)
            .map(DetailedEventDto::getId, DetailedEventDto::getTitle)
            .contains(Assertions.tuple(-9L, "testFestival2"))
            .contains(Assertions.tuple(-10L, "testConcert2"));
        assertEquals(1, foundPerformances.size());
        assertThat(foundPerformances)
            .map(DetailedPerformanceDto::getId, DetailedPerformanceDto::getTitle)
            .contains(Assertions.tuple(-2L, "testPerformance2"));
        assertEquals(0, foundLocations.size());
        assertEquals(1, foundArtists.size());
        assertThat(foundArtists)
            .contains("testPerformer2");

    }

    @Test
    public void searchWithOnlyLocationParamsGivenInParametersShouldOnlyReturnMatchingLocations() {
        SearchDto params = new SearchDto();
        params.setSearchBar("Ernst-Happel-Stadion");
        params.setStreet("Prater1");
        params.setCity("Vienna");
        params.setCountry("Austria");
        params.setZip("1020");

        SearchResultsDto found = eventService.search(params);
        List<DetailedEventDto> foundEvents = found.getEvents();
        List<DetailedPerformanceDto> foundPerformances = found.getPerformances();
        List<SimpleLocationDto> foundLocations = found.getLocations();
        List<String> foundArtists = found.getArtists();
        assertNull(foundEvents);
        assertNull(foundPerformances);
        assertNull(foundArtists);
        assertEquals(1, foundLocations.size());
        assertThat(foundLocations)
            .map(SimpleLocationDto::getId, SimpleLocationDto::getStreet, SimpleLocationDto::getName)
            .contains(Assertions.tuple(-1L, "Prater1", "Ernst-Happel-Stadion"));

    }

    @Test
    public void searchWithMixedParamsGivenInParametersShouldOnlyReturnMatchingEvents() {
        SearchDto params = new SearchDto();
        params.setStreet("Prater1");
        params.setEventName("testConcert");
        params.setStart(LocalDate.of(2000, 9, 27));
        params.setEnd(LocalDate.of(2024, 9, 28));
        SearchResultsDto found = eventService.search(params);
        List<DetailedEventDto> foundEvents = found.getEvents();
        List<DetailedPerformanceDto> foundPerformances = found.getPerformances();
        List<SimpleLocationDto> foundLocations = found.getLocations();
        List<String> foundArtists = found.getArtists();
        assertEquals(1, foundEvents.size());
        assertThat(foundEvents)
            .map(DetailedEventDto::getId, DetailedEventDto::getTitle)
            .contains(Assertions.tuple(-1L, "testConcert"));
        assertNull(foundPerformances);
        assertNull(foundArtists);
        assertNull(foundLocations);
    }

    @Test
    public void findEventsByPerformerNameThrowsNotFoundIfNoSuchEventExists() {
        NotFoundException exception = assertThrows(NotFoundException.class, () -> eventService.findEventsByPerformerName("notPErsistent"));
        assertTrue(exception.getMessage().contains("Could not find an event where performer with name notPErsistent plays"));
    }

    @Test
    public void findEventsByLocationNameThrowsNotFoundIfNoSuchEventExists() {
        NotFoundException exception = assertThrows(NotFoundException.class, () -> eventService.findEventsByLocation("notPErsistent"));
        assertTrue(exception.getMessage().contains("Could not find an event where a performance is at notPErsistent"));
    }

    @Test
    public void findEventsByLocationNameWithValidLocationNameReturnsCorrectEvents() {
        List<DetailedEventDto> events = eventService.findEventsByLocation("Allianz Arena");
        assertEquals(1, events.size());
        assertThat(events)
            .map(DetailedEventDto::getId, DetailedEventDto::getTitle)
            .contains(Assertions.tuple(-10L, "testConcert2"));
    }


}
