package at.ac.tuwien.sepm.groupphase.backend.unittests.performance;

import at.ac.tuwien.sepm.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.*;
import at.ac.tuwien.sepm.groupphase.backend.entity.enums.EventType;
import at.ac.tuwien.sepm.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepm.groupphase.backend.service.PerformanceService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.*;

//TODO: refactor because of changes with seatmap
@ExtendWith(SpringExtension.class)
// This test slice annotation is used instead of @SpringBootTest to load only repository beans instead of
// the entire application context
@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "classpath:sql/insertEventTestData.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class PerformanceServiceTest implements TestData {

    @Autowired
    PerformanceService performanceService;

    @Test
    public void findPerformanceByIdReturnsCorrectPerformance() {
        DetailedPerformanceDto returned = performanceService.findById(-1L);
        assertNotNull(returned);
        assertAll(
            () -> assertEquals(-1L, returned.getId()),
            () -> assertEquals("testPerformance1", returned.getTitle()),
            () -> assertEquals(LocalDateTime.parse("2022-05-22T23:00:00"), returned.getStartTime()),
            () -> assertEquals(LocalDateTime.parse("2023-05-20T12:00:00"), returned.getEndTime()),
            () -> assertEquals(-1L, returned.getEvent().getId()),
            () -> assertEquals(-1L, returned.getLocation().getId())
        );
    }

    @Test
    public void findAllPerformancesReturnsAllPerformances() {
        List<DetailedPerformanceDto> allPerformances = performanceService.findAll();
        assertThat(allPerformances).hasSize(3);
        assertThat(allPerformances)
            .map(DetailedPerformanceDto::getId, DetailedPerformanceDto::getTitle)
            .contains(Assertions.tuple(-1L, "testPerformance1"))
            .contains(Assertions.tuple(-2L, "testPerformance2"))
            .contains(Assertions.tuple(-3L, "testPerformance3"));
    }

    @Test
    public void getByIdShouldThrowNotFoundWhenPerformanceIsDeleteBefore() {
        DetailedPerformanceDto returned = performanceService.findById(-3L);
        assertNotNull(returned);
        assertAll(
            () -> assertEquals(-3L, returned.getId()),
            () -> assertEquals("testPerformance3", returned.getTitle()),
            () -> assertEquals(LocalDateTime.parse("2023-05-15T12:00:00"), returned.getStartTime()),
            () -> assertEquals(LocalDateTime.parse("2023-05-17T12:00:00"), returned.getEndTime()),
            () -> assertEquals(-10L, returned.getEvent().getId()),
            () -> assertEquals(-2L, returned.getLocation().getId())
        );
        performanceService.deleteById(-3L);
        assertThatExceptionOfType(NotFoundException.class).isThrownBy(() -> {
            performanceService.findById(-3L);
        });
    }

    @Test
    public void createPerformanceWithValidDataShouldReturnNewlyCreatedPerformance() throws ValidationException, ConflictException {
        SimpleLocationDto locationDto = new SimpleLocationDto(-1L, "Ernst-Happel-Stadion", "Austria", "Vienna", "1020", "Prater1", "Football Stadium with Capacity of app. 50k");
        SimpleEventDto eventDto = new SimpleEventDto(-1L, "testConcert", EventType.CONCERT, LocalDate.parse("2000-09-28"), LocalDate.parse("2023-09-28"), TEST_EVENT_IMAGE);
        List<String> performers = new ArrayList<>();
        performers.add("p1");
        performers.add("p2");
        performers.add("p3");
        CreatePerformanceDto toCreate = new CreatePerformanceDto("created", locationDto, eventDto, performers, LocalDateTime.parse("2022-05-22T23:00:00"), LocalDateTime.parse("2023-05-22T23:00:00"), BigDecimal.valueOf(100.00), null);
        DetailedPerformanceDto created = performanceService.createPerformance(toCreate);
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
    public void updateWithInvalidDataThrowsValidationException() {
        SimpleLocationDto locationDto = new SimpleLocationDto(-1L, "Ernst-Happel-Stadion", "Austria", "Vienna", "1020", "Prater1", "Football Stadium with Capacity of app. 50k");
        SimpleEventDto eventDto = new SimpleEventDto(-1L, "testConcert", EventType.CONCERT, LocalDate.parse("2000-09-28"), LocalDate.parse("2023-09-28"), TEST_EVENT_IMAGE);
        List<String> performers = new ArrayList<>();
        performers.add("testPerformer1");
        performers.add("testPerformer2");
        DetailedPerformanceDto toChange = new DetailedPerformanceDto(-1L, "", locationDto, eventDto, performers, LocalDateTime.parse("2022-05-22T23:00:00"), LocalDateTime.parse("2023-05-20T12:00:00"), BigDecimal.valueOf(100.00), null);
        assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> {
            performanceService.updatePerformance(-1L, toChange);
        });
    }
}