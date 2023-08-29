package at.ac.tuwien.sepm.groupphase.backend.unittests.location;


import at.ac.tuwien.sepm.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CreateLocationDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.SimpleLocationDto;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.service.LocationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
// This test slice annotation is used instead of @SpringBootTest to load only repository beans instead of
// the entire application context
@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "classpath:sql/insertEventTestData.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class LocationServiceTest implements TestData {

    @Autowired
    private LocationService locationService;

    @Test
    public void findAllReturnListOfLocationDtosWithAllLocations() {
        List<SimpleLocationDto> allLocations = locationService.findAll(0, 0);

        assertEquals(3, allLocations.size());
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
    public void GivenValidIdFindOneReturnsLocationDtoWithSpecifiedIdAndMatchingProperties() {
        SimpleLocationDto locationDto = locationService.findOne(-1L);

        assertNotNull(locationDto);
        assertAll(
            () -> assertEquals(-1L, locationDto.getId()),
            () -> assertEquals("Ernst-Happel-Stadion", locationDto.getName()),
            () -> assertEquals("Austria", locationDto.getCountry()),
            () -> assertEquals("Vienna", locationDto.getCity()),
            () -> assertEquals("Prater1", locationDto.getStreet()),
            () -> assertEquals("1020", locationDto.getPostalCode()),
            () -> assertEquals("Football Stadium with Capacity of app. 50k", locationDto.getDescription())
        );

    }

    @Test
    public void createLocationShouldReturnLocationDtoWithCreatedProperties() {
        CreateLocationDto createLocationDto = new CreateLocationDto(
            TEST_LOCATION_NAME,
            TEST_LOCATION_COUNTRY,
            TEST_LOCATION_CITY,
            TEST_LOCATION_POSTAL_CODE,
            TEST_LOCATION_STREET,
            TEST_LOCATION_DESCRIPTION);

        SimpleLocationDto createdLocation = locationService.createLocation(createLocationDto);
        assertNotNull(createdLocation);
        assertAll(
            () -> assertNotNull(createdLocation.getId()),
            () -> assertEquals(createLocationDto.getName(), createdLocation.getName()),
            () -> assertEquals(createLocationDto.getCountry(), createdLocation.getCountry()),
            () -> assertEquals(createLocationDto.getCity(), createdLocation.getCity()),
            () -> assertEquals(createLocationDto.getStreet(), createdLocation.getStreet()),
            () -> assertEquals(createLocationDto.getPostalCode(), createdLocation.getPostalCode()),
            () -> assertEquals(createLocationDto.getDescription(), createdLocation.getDescription())
        );
    }

    @Test
    public void updateLocationWithValidLocationDtoShouldReturnUpdatedLocationWithMatchingProperties() {
        SimpleLocationDto toUpdate = new SimpleLocationDto(
            -1L,
            "Updated Name",
            "Updated Country",
            "Vienna",
            "Prater1",
            "1020",
            "Football Stadium with Capacity of app. 50k");

        SimpleLocationDto updatedLocation = locationService.updateLocation(-1L, toUpdate);
        assertNotNull(updatedLocation);
        assertAll(
            () -> assertEquals(-1L, updatedLocation.getId()),
            () -> assertEquals("Updated Name", updatedLocation.getName()),
            () -> assertEquals("Updated Country", updatedLocation.getCountry()),
            () -> assertEquals("Vienna", updatedLocation.getCity()),
            () -> assertEquals("1020", updatedLocation.getStreet()),
            () -> assertEquals("Prater1", updatedLocation.getPostalCode()),
            () -> assertEquals("Football Stadium with Capacity of app. 50k", updatedLocation.getDescription())
        );
    }

    @Test
    public void findOneWithInvalidIdShouldThrowNotFoundException() {
        NotFoundException exception = assertThrows(NotFoundException.class, () -> locationService.findOne(1984L));
        assertTrue(exception.getMessage().contains("Could not find location with id 1984"));
    }

    @Test
    public void updateLocationWithInvalidIdShouldThrowNotFoundException() {
        SimpleLocationDto toUpdate = new SimpleLocationDto(
            1984L,
            "Updated Name",
            "Updated Country",
            "Vienna",
            "Prater1",
            "1020",
            "Football Stadium with Capacity of app. 50k");

        NotFoundException exception = assertThrows(NotFoundException.class, () -> locationService.updateLocation(1984L, toUpdate));
        assertTrue(exception.getMessage().contains("Could not find location with id 1984"));
    }

    @Test
    public void deleteLocationWithValidIdShouldDeleteLocation() {
        locationService.deleteLocation(-3L);
        List<SimpleLocationDto> locations = locationService.findAll(0, 0);
        assertEquals(2, locations.size());
        assertThat(locations)
            .map(SimpleLocationDto::getId, SimpleLocationDto::getName)
            .contains(tuple(-2L, "Allianz Arena"))
            .contains(tuple(-1L, "Ernst-Happel-Stadion"));
    }

    @Test
    public void deleteLocationWithInvalidIdShouldThrowNotFoundException() {
        NotFoundException exception = assertThrows(NotFoundException.class, () -> locationService.deleteLocation(-1984L));
        assertTrue(exception.getMessage().contains("Could not find location with id -1984"));
    }
}
