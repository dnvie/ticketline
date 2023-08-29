package at.ac.tuwien.sepm.groupphase.backend.integrationtest;


import at.ac.tuwien.sepm.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepm.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedCreateSeatDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedSeatDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.SimpleSeatDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.SimpleSectorDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper.SeatMapper;
import at.ac.tuwien.sepm.groupphase.backend.entity.enums.SectorType;
import at.ac.tuwien.sepm.groupphase.backend.repository.SeatRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.TicketRepository;
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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Sql(scripts = "classpath:sql/insertEventTestData.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class SeatEndpointTest implements TestData {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SeatMapper seatMapper;

    @Autowired
    private JwtTokenizer jwtTokenizer;

    @Autowired
    private SecurityProperties securityProperties;

    @Test
    public void getAllSeatsShouldReturnListSizeThree() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .get(SEAT_BASE_URI).header(securityProperties.getAuthHeader(),
                    jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());

        List<SimpleSeatDto> simpleSeatDtos = Arrays.asList(objectMapper.readValue(response.getContentAsString(), SimpleSeatDto[].class));
        assertNotNull(simpleSeatDtos);
        assertEquals(3, simpleSeatDtos.size());
        assertThat(simpleSeatDtos)
            .map(SimpleSeatDto::getId, SimpleSeatDto::getNumber)
            .contains(
                tuple(UUID.fromString("b1c9473d-02a1-437f-aaaf-8828e0093716"), 1),
                tuple(UUID.fromString("f344be21-8a26-467a-a86a-19e6ca65996e"), 1),
                tuple(UUID.fromString("a2b3de22-8a26-467a-a86a-19e6ca65996e"), 2)
            );
    }

    @Test
    public void getAllSeatsWithNoSeatsShouldReturnListWithSizeZero() throws Exception {
        //delete tickets because of FK constraint
        ticketRepository.deleteAll();
        seatRepository.deleteAll();
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .get(SEAT_BASE_URI).header(securityProperties.getAuthHeader(),
                    jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());

        List<SimpleSeatDto> simpleSeatDtos = Arrays.asList(objectMapper.readValue(response.getContentAsString(), SimpleSeatDto[].class));
        assertNotNull(simpleSeatDtos);
        assertEquals(0, simpleSeatDtos.size());
    }

    @Test
    public void getSeatByIdWithValidIdShouldReturnCorrectSeat() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .get(SEAT_BASE_URI + "/b1c9473d-02a1-437f-aaaf-8828e0093716").header(securityProperties.getAuthHeader(),
                    jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());

        SimpleSeatDto simpleSeatDto = objectMapper.readValue(response.getContentAsString(), SimpleSeatDto.class);
        assertNotNull(simpleSeatDto);
        assertAll(
            () -> assertEquals(UUID.fromString("b1c9473d-02a1-437f-aaaf-8828e0093716"), simpleSeatDto.getId()),
            () -> assertEquals(1, simpleSeatDto.getNumber()),
            () -> assertEquals(0, simpleSeatDto.getSeatColumn()),
            () -> assertEquals(0, simpleSeatDto.getSeatRow())
            //TODO: maybe add sector but currently not used in dto
        );
    }

    @Test
    public void getSeatByIdWithInvalidIdShouldReturn404() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .get(SEAT_BASE_URI + "/19841234-1984-1984-1984-123456789101").header(securityProperties.getAuthHeader(),
                    jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
    }

    @Test
    public void createSeatWithValidSeatShouldReturn201AndCreatedSeat() throws Exception {
        SimpleSectorDto simpleSectorDto = SimpleSectorDto.builder()
            .id(UUID.fromString("ad2303fe-85fc-4132-a014-ef6a4ed4caf5"))
            .seatMapColumn(0)
            .seatMapRow(0)
            .price(BigDecimal.valueOf(1.5))
            .lodgeSize(0)
            .name("test sector1")
            .orientation(25)
            .noUpdate(false)
            .type(SectorType.REGULAR)
            .standingSector(false)
            .length(1)
            .width(1)
            .build();
        DetailedCreateSeatDto toCreate = DetailedCreateSeatDto.builder().seatRow(1).seatColumn(0).number(2).sector(simpleSectorDto).build();
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .post(SEAT_BASE_URI).header(securityProperties.getAuthHeader(),
                    jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(toCreate)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.CREATED.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());

        DetailedSeatDto createdSeatDto = objectMapper.readValue(response.getContentAsString(), DetailedSeatDto.class);
        assertNotNull(createdSeatDto);
        assertAll(
            () -> assertNotNull(createdSeatDto.getId()),
            () -> assertEquals(2, createdSeatDto.getNumber()),
            () -> assertEquals(0, createdSeatDto.getSeatColumn()),
            () -> assertEquals(1, createdSeatDto.getSeatRow()),
            () -> assertEquals(simpleSectorDto.getId(), createdSeatDto.getSector().getId()),
            () -> assertEquals(simpleSectorDto.getSeatMapRow(), createdSeatDto.getSector().getSeatMapRow()),
            () -> assertEquals(simpleSectorDto.getSeatMapColumn(), createdSeatDto.getSector().getSeatMapColumn()),
            () -> assertEquals(simpleSectorDto.getOrientation(), createdSeatDto.getSector().getOrientation()),
            () -> assertEquals(simpleSectorDto.getLodgeSize(), createdSeatDto.getSector().getLodgeSize()),
            () -> assertEquals(simpleSectorDto.getName(), createdSeatDto.getSector().getName()),
            () -> assertEquals(simpleSectorDto.getType(), createdSeatDto.getSector().getType()),
            () -> assertEquals(simpleSectorDto.getPrice(), createdSeatDto.getSector().getPrice()),
            () -> assertEquals(simpleSectorDto.getNoUpdate(), createdSeatDto.getSector().getNoUpdate()),
            () -> assertEquals(simpleSectorDto.getStandingSector(), createdSeatDto.getSector().getStandingSector())
        );
    }
}
