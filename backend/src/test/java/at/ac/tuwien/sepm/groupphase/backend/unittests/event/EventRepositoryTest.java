package at.ac.tuwien.sepm.groupphase.backend.unittests.event;

import at.ac.tuwien.sepm.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepm.groupphase.backend.entity.Event;
import at.ac.tuwien.sepm.groupphase.backend.entity.Performance;
import at.ac.tuwien.sepm.groupphase.backend.repository.EventRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
// This test slice annotation is used instead of @SpringBootTest to load only repository beans instead of
// the entire application context
@DataJpaTest
@ActiveProfiles("test")
@Sql(scripts = "classpath:sql/insertEventTestData.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class EventRepositoryTest implements TestData {

    @Autowired
    private EventRepository eventRepository;

    @Test
    public void givenNothing_whenFindAll_thenFindListWithAllEvents() {
        List<Event> events = eventRepository.findAll();
        assertThat(events).hasSize(10);
        assertThat(events)
            .map(Event::getId, Event::getTitle)
            .contains(Assertions.tuple(-1L, "testConcert"))
            .contains(Assertions.tuple(-2L, "testFestival"))
            .contains(Assertions.tuple(-3L, "testTheater"))
            .contains(Assertions.tuple(-4L, "testOpera"))
            .contains(Assertions.tuple(-5L, "testMusical"))
            .contains(Assertions.tuple(-6L, "testMovie"))
            .contains(Assertions.tuple(-7L, "testOther"))
            .contains(Assertions.tuple(-8L, "testOther"))
            .contains(Assertions.tuple(-9L, "testFestival2"))
            .contains(Assertions.tuple(-10L, "testConcert2"));
    }

    @Test
    public void givenNothing_whenFindEventByTitle_thenFindListWithMatchingTitle() {
        List<Event> events = eventRepository.findEventByTitle("testOther");
        assertThat(events).hasSize(2);
        assertThat(events)
            .map(Event::getId, Event::getTitle)
            .contains(Assertions.tuple(-7L, "testOther"))
            .contains(Assertions.tuple(-8L, "testOther"));
    }

    @Test
    public void givenNothing_whenFindAllByTitleContainingIgnoreCase_thenFindListWithAllEventsMatchingGivenTitle() {
        List<Event> events = eventRepository.findAllByTitleContainingIgnoreCase("testConcert");
        assertThat(events).hasSize(2);
        assertThat(events)
            .map(Event::getId, Event::getTitle)
            .contains(Assertions.tuple(-1L, "testConcert"))
            .contains(Assertions.tuple(-10L, "testConcert2"));
    }

    @Test
    public void givenNothing_whenFindAllWithPerformancesAndLocation_thenFindListWithAllEventsAndPerformancesAndLocations() {
        Pageable pageable = Pageable.ofSize(10);
        List<Event> events = eventRepository.findAllWithPerformancesAndLocation(pageable);
        assertThat(events).hasSize(10);

        //Assert that event 1 and 2 have performances
        assertThat(events.get(0).getPerformances()).hasSize(1);
        assertThat(events.get(8).getPerformances()).hasSize(1);
        Set<Performance> performancesEvent1 = events.get(0).getPerformances();
        Set<Performance> performancesEvent2 = events.get(8).getPerformances();

        //assert that correct performances are in event
        assertThat(performancesEvent1)
            .map(Performance::getId, Performance::getTitle)
            .contains(Assertions.tuple(-1L, "testPerformance1"));
        assertThat(performancesEvent2)
            .map(Performance::getId, Performance::getTitle)
            .contains(Assertions.tuple(-2L, "testPerformance2"));

        //assert that performances have location
        assertThat(performancesEvent1.iterator().next().getLocation().getId()).isEqualTo(-1L);
        assertThat(performancesEvent2.iterator().next().getLocation().getId()).isEqualTo(-1L);
    }

    @Test
    public void givenNothing_whenDeleteEventByID_thenReturnOne() {
        Long deleted = eventRepository.deleteEventById(-3L);
        assertEquals(1, deleted);
        List<Event> events = eventRepository.findAll();
        assertThat(events).hasSize(9);
    }

    @Test
    public void givenNothing_whenFindByIdWithPerformancesAndLocation_thenFindEventWithPerformancesAndLocations() {
        Optional<Event> eventByOptional = eventRepository.findByIdWithPerformancesAndLocation(-1L);
        assertThat(eventByOptional).isPresent();
        Event event = eventByOptional.get();
        assertEquals(-1L, event.getId());
        assertEquals("testConcert", event.getTitle());
        assertEquals(1, event.getPerformances().size());
        assertThat(event.getPerformances())
            .map(Performance::getId, Performance::getTitle)
            .contains(Assertions.tuple(-1L, "testPerformance1"));
        assertEquals(-1L, event.getPerformances().iterator().next().getLocation().getId());
    }

    @Test
    public void givenNothing_whenFindAllPerformers_thenReturnListWithAllPerformers() {
        List<String> performers = eventRepository.findAllPerformers(-2L);
        assertThat(performers).hasSize(3);
        assertThat(performers)
            .contains("testPerformer3")
            .contains("testPerformer4")
            .contains("testPerformer5");
    }

    @Test
    public void givenNothing_whenFindAllPerformersByName_thenReturnListWithAllPerformersMatchingName() {
        List<String> performers = eventRepository.findAllPerformersByName("1");
        assertThat(performers).hasSize(1);
        assertThat(performers)
            .contains("testPerformer1");
    }

    @Test
    public void givenNothing_whenFindEventsMatchingPerformerName_thenReturnListWithAllEventsWherePerformerPerforms() {
        List<Event> events = eventRepository.findEventsMatchingToPerformerName("testPerformer1");
        assertThat(events).hasSize(1);
        assertThat(events)
            .map(Event::getId, Event::getTitle)
            .contains(Assertions.tuple(-1L, "testConcert"));
    }

    @Test
    public void givenNothing_whenFindEventsMatchingLocationName_thenReturnListWithAllEventsAtLocation() {
        List<Event> events = eventRepository.findEventsMatchingToLocation("Ernst-Happel-Stadion");
        assertThat(events).hasSize(2);
        assertThat(events)
            .map(Event::getId, Event::getTitle)
            .contains(Assertions.tuple(-1L, "testConcert"))
            .contains(Assertions.tuple(-2L, "testFestival"));
    }

    @Test
    public void givenNothing_whenFindEventsByPartOfTitle_thenReturnListWithAllMatchingEvents() {
        List<Event> events = eventRepository.findEventsByPartOfTitle("testConcert");
        assertThat(events).hasSize(2);
        assertThat(events)
            .map(Event::getId, Event::getTitle)
            .contains(Assertions.tuple(-1L, "testConcert"))
            .contains(Assertions.tuple(-10L, "testConcert2"));
    }


}