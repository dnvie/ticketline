package at.ac.tuwien.sepm.groupphase.backend.unittests.performance;

import at.ac.tuwien.sepm.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepm.groupphase.backend.entity.Performance;
import at.ac.tuwien.sepm.groupphase.backend.repository.PerformanceRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
// This test slice annotation is used instead of @SpringBootTest to load only repository beans instead of
// the entire application context
@DataJpaTest
@ActiveProfiles("test")
@Sql(scripts = "classpath:sql/insertEventTestData.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class PerformanceRepositoryTest implements TestData {
    @Autowired
    PerformanceRepository performanceRepository;

    @Test
    public void findAllReturnsAllPerformances() {
        List<Performance> allPerformances = performanceRepository.findAll();
        assertThat(allPerformances).hasSize(3);
        assertThat(allPerformances)
            .map(Performance::getId, Performance::getTitle)
            .contains(Assertions.tuple(-1L, "testPerformance1"))
            .contains(Assertions.tuple(-2L, "testPerformance2"))
            .contains(Assertions.tuple(-3L, "testPerformance3"));
    }

    @Test
    public void givenNothing_whenFindPerformancesByNamePart_thenReturnListWithAllMatchingPerformances() {
        List<Performance> performances = performanceRepository.findPerformancesByNamePart("estperf");
        assertThat(performances).hasSize(3);
        assertThat(performances)
            .map(Performance::getId, Performance::getTitle)
            .contains(Assertions.tuple(-1L, "testPerformance1"))
            .contains(Assertions.tuple(-2L, "testPerformance2"))
            .contains(Assertions.tuple(-3L, "testPerformance3"));
    }
}
