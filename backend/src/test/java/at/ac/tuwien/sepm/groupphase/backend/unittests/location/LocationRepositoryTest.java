package at.ac.tuwien.sepm.groupphase.backend.unittests.location;

import at.ac.tuwien.sepm.groupphase.backend.entity.Location;
import at.ac.tuwien.sepm.groupphase.backend.repository.LocationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(SpringExtension.class)
// This test slice annotation is used instead of @SpringBootTest to load only repository beans instead of
// the entire application context
@DataJpaTest
@ActiveProfiles("test")
@Sql(scripts = "classpath:sql/insertEventTestData.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class LocationRepositoryTest {
    @Autowired
    LocationRepository locationRepository;

    @Test
    public void findAllReturnsAllLocations() {
        List<Location> allLocations = locationRepository.findAll();
        assertThat(allLocations).hasSize(3);
        assertAll(
            () -> assertEquals(-1L, allLocations.get(2).getId()),
            () -> assertEquals("Ernst-Happel-Stadion", allLocations.get(2).getName()),
            () -> assertEquals("Austria", allLocations.get(2).getCountry()),
            () -> assertEquals("Vienna", allLocations.get(2).getCity()),
            () -> assertEquals("Prater1", allLocations.get(2).getStreet()),
            () -> assertEquals("1020", allLocations.get(2).getPostalCode()),
            () -> assertEquals("Football Stadium with Capacity of app. 50k", allLocations.get(2).getDescription()),
            () -> assertEquals(-2L, allLocations.get(1).getId()),
            () -> assertEquals("Allianz Arena", allLocations.get(1).getName()),
            () -> assertEquals("Germany", allLocations.get(1).getCountry()),
            () -> assertEquals("Munich", allLocations.get(1).getCity()),
            () -> assertEquals("Froettmaning 2", allLocations.get(1).getStreet()),
            () -> assertEquals("100000", allLocations.get(1).getPostalCode()),
            () -> assertEquals("Football Stadium with Capacity of app. 50k", allLocations.get(1).getDescription()),
            () -> assertEquals(-3L, allLocations.get(0).getId()),
            () -> assertEquals("test", allLocations.get(0).getName()),
            () -> assertEquals("test", allLocations.get(0).getCountry()),
            () -> assertEquals("test", allLocations.get(0).getCity()),
            () -> assertEquals("street1", allLocations.get(0).getStreet()),
            () -> assertEquals("100000", allLocations.get(0).getPostalCode()),
            () -> assertEquals("cool location", allLocations.get(0).getDescription())
        );
    }

    @Test
    public void findOneByIdReturnsLocationById() {
        Location location = locationRepository.findLocationById(-1L);
        assertNotNull(location);
        assertAll(
            () -> assertEquals(-1L, location.getId()),
            () -> assertEquals("Ernst-Happel-Stadion", location.getName()),
            () -> assertEquals("Austria", location.getCountry()),
            () -> assertEquals("Vienna", location.getCity()),
            () -> assertEquals("Prater1", location.getStreet()),
            () -> assertEquals("1020", location.getPostalCode()),
            () -> assertEquals("Football Stadium with Capacity of app. 50k", location.getDescription())
        );
    }

    @Test
    public void findLocationByNameReturnsAllLocationsWithGivenName() {
        List<Location> locations = locationRepository.findLocationByName("Ernst-Happel-Stadion");
        assertNotNull(locations);
        assertEquals(1, locations.size());
        assertAll(
            () -> assertEquals(-1L, locations.get(0).getId()),
            () -> assertEquals("Ernst-Happel-Stadion", locations.get(0).getName()),
            () -> assertEquals("Austria", locations.get(0).getCountry()),
            () -> assertEquals("Vienna", locations.get(0).getCity()),
            () -> assertEquals("Prater1", locations.get(0).getStreet()),
            () -> assertEquals("1020", locations.get(0).getPostalCode()),
            () -> assertEquals("Football Stadium with Capacity of app. 50k", locations.get(0).getDescription())
        );
    }

    @Test
    public void deleteLocationByValidIdShouldReturnOne() {
        Long deleted = locationRepository.deleteLocationById(-3L);
        assertEquals(1L, deleted);
        List<Location> locations = locationRepository.findAll();
        assertThat(locations).hasSize(2);
    }

    @Test
    public void deleteLocationByInvalidIdShouldReturnZero() {
        Long deleted = locationRepository.deleteLocationById(-1984L);
        assertEquals(0L, deleted);
        List<Location> locations = locationRepository.findAll();
        assertThat(locations).hasSize(3);

    }


}
