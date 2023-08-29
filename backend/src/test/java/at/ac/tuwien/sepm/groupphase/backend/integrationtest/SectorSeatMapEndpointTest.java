package at.ac.tuwien.sepm.groupphase.backend.integrationtest;

import at.ac.tuwien.sepm.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepm.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CreateSeatDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CreateSeatMapDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CreateSectorDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedSeatMapDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.SimpleMessageDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.SimpleSeatMapsWithCountDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper.SectorMapper;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper.UserMapper;
import at.ac.tuwien.sepm.groupphase.backend.entity.enums.SectorType;
import at.ac.tuwien.sepm.groupphase.backend.repository.SeatMapRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.SectorRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepm.groupphase.backend.security.JwtTokenizer;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.RequestEntity.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Sql(scripts = "classpath:sql/insertEventTestData.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class SectorSeatMapEndpointTest implements TestData {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SeatMapRepository seatMapRepository;

    @Autowired
    private SectorRepository sectorRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SectorMapper sectorMapper;

    @Autowired
    private JwtTokenizer jwtTokenizer;

    @Autowired
    private SecurityProperties securityProperties;

    @Test
    public void getAllSeatMapsWithNoParamsShouldReturnTwoSeatMaps() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(SEATMAPS_SECTOR_BASE_URI)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());

        SimpleSeatMapsWithCountDto seatMapsWithCountDto = objectMapper.readValue(response.getContentAsString(),
            SimpleSeatMapsWithCountDto.class);
        assertNotNull(seatMapsWithCountDto);
        assertEquals(2, seatMapsWithCountDto.getTotalCount());
        assertAll(
            () -> assertEquals(UUID.fromString("24e55098-9e8b-4d7a-b573-5dcd752a8097"), seatMapsWithCountDto.getSeatMaps().get(0).getId()),
            () -> assertEquals(UUID.fromString("876af6ba-a75b-4f1b-ab6a-03f6864ee44e"), seatMapsWithCountDto.getSeatMaps().get(1).getId())
        );

    }

    @Test
    public void getSeatMapByIdWithValidIdShouldReturnSeatMapWithAllProperties() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(SEATMAPS_SECTOR_BASE_URI + "/{id}", UUID.fromString("24e55098-9e8b-4d7a-b573-5dcd752a8097"))
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());

        DetailedSeatMapDto detailedSeatMapDto = objectMapper.readValue(response.getContentAsString(),
            DetailedSeatMapDto.class);
        assertNotNull(detailedSeatMapDto);
        assertAll(
            () -> assertEquals(UUID.fromString("24e55098-9e8b-4d7a-b573-5dcd752a8097"), detailedSeatMapDto.getId()),
            () -> assertEquals("testSeatMap1", detailedSeatMapDto.getName()),
            () -> assertEquals(1, detailedSeatMapDto.getSectors().size()),
            () -> assertEquals(0, detailedSeatMapDto.getSectors().stream().iterator().next().getSeatMapRow()),
            () -> assertEquals(0, detailedSeatMapDto.getSectors().stream().iterator().next().getSeatMapColumn())
        );

    }

    @Test
    public void getSeatMapByIdWithInvalidIdShouldReturnNotFound() throws Exception {
        this.mockMvc.perform(get(SEATMAPS_SECTOR_BASE_URI + "/{id}", UUID.fromString("19485098-9e8b-4d7a-b573-5dcd752a8098"))
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andExpect(status().isNotFound()).andDo(print());
    }

    @Test
    public void createSeatMapWithValidSeatMapShouldReturnCreatedSeatMap() throws Exception {
        Set<CreateSeatDto> seatSet = new HashSet<>();
        seatSet.add(new CreateSeatDto(1, 1, 1));
        Set<CreateSectorDto> sectorSet = new HashSet<>();
        sectorSet.add(
            new CreateSectorDto(SectorType.LODGE, seatSet, "TestSector2", BigDecimal.valueOf(1.5),
                0, 0, 0, 0, false, false, 1, 1));

        CreateSeatMapDto createSeatMapDto = CreateSeatMapDto.builder()
            .name("testSeatMap3")
            .sectors(sectorSet)
            .build();

        MvcResult mvcResult = this.mockMvc.perform(get(SEATMAPS_SECTOR_BASE_URI)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        SimpleSeatMapsWithCountDto seatMapsWithCountDto = objectMapper.readValue(response.getContentAsString(),
            SimpleSeatMapsWithCountDto.class);
        assertNotNull(seatMapsWithCountDto);
        Long seatMapCount = seatMapsWithCountDto.getTotalCount();

        mvcResult = this.mockMvc.perform(MockMvcRequestBuilders
                .post(SEATMAPS_SECTOR_BASE_URI).contentType(MediaType.APPLICATION_JSON)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(DEFAULT_ADMIN_USER_ID, ADMIN_ROLES))
                .content(objectMapper.writeValueAsString(createSeatMapDto)))
            .andDo(print())
            .andReturn();
        response = mvcResult.getResponse();

        assertEquals(HttpStatus.CREATED.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());

        DetailedSeatMapDto createdSeatMapDto = objectMapper.readValue(response.getContentAsString(),
            DetailedSeatMapDto.class);
        assertNotNull(createdSeatMapDto);
        assertAll(
            () -> assertEquals(seatMapCount + 1, seatMapRepository.count()),
            () -> assertEquals("testSeatMap3", createdSeatMapDto.getName()),
            () -> assertEquals(1, createdSeatMapDto.getSectors().size()),
            () -> assertEquals(0, createdSeatMapDto.getSectors().stream().iterator().next().getSeatMapRow()),
            () -> assertEquals(0, createdSeatMapDto.getSectors().stream().iterator().next().getSeatMapColumn())
        );
    }

}
