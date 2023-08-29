package at.ac.tuwien.sepm.groupphase.backend.unittests.seatmaps;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedSectorDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.SimpleSectorDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper.SectorMapper;
import at.ac.tuwien.sepm.groupphase.backend.entity.Sector;
import at.ac.tuwien.sepm.groupphase.backend.entity.enums.SectorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;


import java.math.BigDecimal;
import java.util.HashSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class SectorMappingTest {

    private final Sector sector = Sector.builder()
        .id(UUID.fromString("f227921b-4605-43a2-8a1d-a617629c55e1"))
        .name("Sector 1")
        .seats(new HashSet<>())
        .noUpdate(false)
        .orientation(0)
        .price(BigDecimal.valueOf(150))
        .seatMapColumn(1)
        .seatMapRow(1)
        .type(SectorType.LODGE)
        .lodgeSize(10)
        .seatMap(null)
        .build();

    @InjectMocks
    private SectorMapper sectorMapper = Mappers.getMapper(SectorMapper.class);

    @Test
    public void simpleSectorDtoToSector() {
        SimpleSectorDto simpleSectorDto =
            new SimpleSectorDto(
                UUID.fromString("f227921b-4605-43a2-8a1d-a617629c55e1"),
                SectorType.LODGE,
                "Sector 1",
                BigDecimal.valueOf(150),
                0,
                10,
                1,
                1,
                false,
                false,
                1,
                2);

        Sector mappedSector = sectorMapper.simpleSectorDtoToSector(simpleSectorDto);
        assertNotNull(mappedSector);
        assertAll(
            () -> assertEquals(sector.getId(), mappedSector.getId()),
            () -> assertEquals(sector.getName(), mappedSector.getName()),
            () -> assertEquals(sector.getNoUpdate(), mappedSector.getNoUpdate()),
            () -> assertEquals(sector.getOrientation(), mappedSector.getOrientation()),
            () -> assertEquals(sector.getPrice(), mappedSector.getPrice()),
            () -> assertEquals(sector.getSeatMapColumn(), mappedSector.getSeatMapColumn()),
            () -> assertEquals(sector.getSeatMapRow(), mappedSector.getSeatMapRow()),
            () -> assertEquals(sector.getType(), mappedSector.getType()),
            () -> assertEquals(sector.getLodgeSize(), mappedSector.getLodgeSize()),
            () -> assertEquals(sector.getSeatMap(), mappedSector.getSeatMap())
        );
    }

    @Test
    public void detailedSectorDtoToSectorDto() {
        DetailedSectorDto detailedSectorDto = DetailedSectorDto.builder()
            .id(UUID.fromString("f227921b-4605-43a2-8a1d-a617629c55e1"))
            .name("Sector 1")
            .seats(new HashSet<>())
            .noUpdate(false)
            .orientation(0)
            .price(BigDecimal.valueOf(150))
            .seatMapColumn(1)
            .seatMapRow(1)
            .type(SectorType.LODGE)
            .lodgeSize(10)
            .build();

        Sector mappedSector = sectorMapper.detailedSectorDtoToSector(detailedSectorDto);
        assertNotNull(mappedSector);
        assertAll(
            () -> assertEquals(sector.getId(), mappedSector.getId()),
            () -> assertEquals(sector.getName(), mappedSector.getName()),
            () -> assertEquals(sector.getNoUpdate(), mappedSector.getNoUpdate()),
            () -> assertEquals(sector.getOrientation(), mappedSector.getOrientation()),
            () -> assertEquals(sector.getPrice(), mappedSector.getPrice()),
            () -> assertEquals(sector.getSeatMapColumn(), mappedSector.getSeatMapColumn()),
            () -> assertEquals(sector.getSeatMapRow(), mappedSector.getSeatMapRow()),
            () -> assertEquals(sector.getType(), mappedSector.getType()),
            () -> assertEquals(sector.getLodgeSize(), mappedSector.getLodgeSize()),
            () -> assertEquals(sector.getSeatMap(), mappedSector.getSeatMap()),
            () -> assertEquals(sector.getSeats().size(), mappedSector.getSeats().size())
        );
    }


}
