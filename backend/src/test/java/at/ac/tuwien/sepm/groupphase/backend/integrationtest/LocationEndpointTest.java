package at.ac.tuwien.sepm.groupphase.backend.integrationtest;


import at.ac.tuwien.sepm.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepm.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.SimpleLocationDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper.LocationMapper;
import at.ac.tuwien.sepm.groupphase.backend.repository.LocationRepository;
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

import java.util.Arrays;
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
public class LocationEndpointTest implements TestData {

    private final String LOCATION_BASE_URI = "/api/v1/locations";

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private LocationMapper locationMapper;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private JwtTokenizer jwtTokenizer;

    @Autowired
    private SecurityProperties securityProperties;

    @Test
    public void findAllShouldReturnListOfAllLocationsWithSizeTwo() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .get(LOCATION_BASE_URI).header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());

        List<SimpleLocationDto> allLocations = Arrays.asList(objectMapper.readValue(response.getContentAsString(), SimpleLocationDto[].class));
        assertNotNull(allLocations);
        assertEquals(3, allLocations.size());
        assertThat(allLocations)
            .map(SimpleLocationDto::getId, SimpleLocationDto::getName)
            .contains(Assertions.tuple(-1L, "Ernst-Happel-Stadion"))
            .contains(Assertions.tuple(-2L, "Allianz Arena"))
            .contains(Assertions.tuple(-3L, "test"));
    }

    @Test
    public void findOneWithValidIdShouldReturnLocationWithAllProperties() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .get(LOCATION_BASE_URI + "/-1").header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());

        SimpleLocationDto locationDto = objectMapper.readValue(response.getContentAsString(), SimpleLocationDto.class);
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
    public void deleteLocationWithValidIdTwiceShouldDeleteAndReturn404() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                .delete(LOCATION_BASE_URI + "/-3").header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andExpect(status().isNoContent()).andDo(print());
        mockMvc.perform(MockMvcRequestBuilders
                .delete(LOCATION_BASE_URI + "/-3").header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andExpect(status().isNotFound()).andDo(print());
    }


}
