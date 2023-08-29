package at.ac.tuwien.sepm.groupphase.backend.unittests.location;


import at.ac.tuwien.sepm.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CreateLocationDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.SimpleLocationDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper.LocationMapper;
import at.ac.tuwien.sepm.groupphase.backend.entity.Location;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class LocationMappingTest implements TestData {
    @Autowired
    private LocationMapper locationMapper;

    private final Location location = new Location(1L,
        TEST_LOCATION_NAME,
        TEST_LOCATION_COUNTRY,
        TEST_LOCATION_CITY,
        TEST_LOCATION_POSTAL_CODE,
        TEST_LOCATION_STREET,
        TEST_LOCATION_DESCRIPTION,
        new HashSet<>());

    @Test
    public void mapLocationEntityToSimpleLocationDtoReturnSimpleLocationDtoWithAllProperties() {

        SimpleLocationDto simpleLocationDto = locationMapper.locationToSimpleLocationDto(location);
        assertNotNull(simpleLocationDto);
        assertAll(
            () -> assertEquals(1L, simpleLocationDto.getId()),
            () -> assertEquals(TEST_LOCATION_NAME, simpleLocationDto.getName()),
            () -> assertEquals(TEST_LOCATION_STREET, simpleLocationDto.getStreet()),
            () -> assertEquals(TEST_LOCATION_DESCRIPTION, simpleLocationDto.getDescription()),
            () -> assertEquals(TEST_LOCATION_COUNTRY, simpleLocationDto.getCountry()),
            () -> assertEquals(TEST_LOCATION_POSTAL_CODE, simpleLocationDto.getPostalCode()),
            () -> assertEquals(TEST_LOCATION_CITY, simpleLocationDto.getCity())
        );
    }

    @Test
    public void mapListOfLocationsToListOfSimpleLocationDtosReturnListOfSimpleLocationDtosWithAllProperties() {
        List<Location> l1 = new ArrayList<>();
        l1.add(location);
        l1.add(location);

        List<SimpleLocationDto> simpleLocationDtos = locationMapper.locationToSimpleLocationDto(l1);

        assertEquals(2, simpleLocationDtos.size());
        assertAll(
            () -> assertEquals(1L, simpleLocationDtos.get(0).getId()),
            () -> assertEquals(TEST_LOCATION_NAME, simpleLocationDtos.get(0).getName()),
            () -> assertEquals(TEST_LOCATION_STREET, simpleLocationDtos.get(0).getStreet()),
            () -> assertEquals(TEST_LOCATION_DESCRIPTION, simpleLocationDtos.get(0).getDescription()),
            () -> assertEquals(TEST_LOCATION_COUNTRY, simpleLocationDtos.get(0).getCountry()),
            () -> assertEquals(TEST_LOCATION_POSTAL_CODE, simpleLocationDtos.get(0).getPostalCode()),
            () -> assertEquals(TEST_LOCATION_CITY, simpleLocationDtos.get(0).getCity()),
            () -> assertEquals(1L, simpleLocationDtos.get(1).getId()),
            () -> assertEquals(TEST_LOCATION_NAME, simpleLocationDtos.get(1).getName()),
            () -> assertEquals(TEST_LOCATION_STREET, simpleLocationDtos.get(1).getStreet()),
            () -> assertEquals(TEST_LOCATION_DESCRIPTION, simpleLocationDtos.get(1).getDescription()),
            () -> assertEquals(TEST_LOCATION_COUNTRY, simpleLocationDtos.get(1).getCountry()),
            () -> assertEquals(TEST_LOCATION_POSTAL_CODE, simpleLocationDtos.get(1).getPostalCode()),
            () -> assertEquals(TEST_LOCATION_CITY, simpleLocationDtos.get(1).getCity())
        );

    }

    @Test
    public void mapLocationToCreateLocationDtoReturnsCreateLocationDtoWithAllProperties() {
        CreateLocationDto createLocationDto = locationMapper.locationToCreateLocationDto(location);
        assertNotNull(createLocationDto);
        assertAll(
            () -> assertEquals(TEST_LOCATION_NAME, createLocationDto.getName()),
            () -> assertEquals(TEST_LOCATION_STREET, createLocationDto.getStreet()),
            () -> assertEquals(TEST_LOCATION_DESCRIPTION, createLocationDto.getDescription()),
            () -> assertEquals(TEST_LOCATION_COUNTRY, createLocationDto.getCountry()),
            () -> assertEquals(TEST_LOCATION_POSTAL_CODE, createLocationDto.getPostalCode()),
            () -> assertEquals(TEST_LOCATION_CITY, createLocationDto.getCity())
        );
    }
}
