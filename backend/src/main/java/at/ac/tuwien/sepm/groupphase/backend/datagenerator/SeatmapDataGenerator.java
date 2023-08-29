package at.ac.tuwien.sepm.groupphase.backend.datagenerator;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CreateSeatDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CreateSeatMapDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CreateSectorDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper.SectorMapper;
import at.ac.tuwien.sepm.groupphase.backend.entity.Seat;
import at.ac.tuwien.sepm.groupphase.backend.entity.SeatMap;
import at.ac.tuwien.sepm.groupphase.backend.entity.Sector;
import at.ac.tuwien.sepm.groupphase.backend.entity.enums.SectorType;
import at.ac.tuwien.sepm.groupphase.backend.repository.SeatMapRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

@Profile("generateData")
@Component
public class SeatmapDataGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final int SEATMAP_COUNT = 100;
    private final SeatMapRepository seatmapRepository;
    private final SectorMapper sectorMapper;

    public SeatmapDataGenerator(SeatMapRepository seatmapRepository, SectorMapper sectorMapper) {
        this.seatmapRepository = seatmapRepository;
        this.sectorMapper = sectorMapper;
    }

    public void generateSeatmaps() {
        if (seatmapRepository.findAll().size() > 1) {
            LOGGER.debug("seatmap already generated");
        } else {
            LOGGER.debug("generating {} seatmap entries", SEATMAP_COUNT);
            for (int i = 0; i < SEATMAP_COUNT; i++) {
                SeatMap seatmap = sectorMapper.createSeatMapDtoToSeatMap(generateSeatmap("Seatmap " + i));
                mapSeatMapToSectorForCreate(seatmap.getSectors(), seatmap);
                seatmapRepository.save(seatmap);
            }
        }
    }

    private CreateSeatMapDto generateSeatmap(String name) {
        ArrayList<Integer> randList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            randList.add(i);
        }
        Collections.shuffle(randList);

        CreateSeatMapDto seatmap = new CreateSeatMapDto();
        seatmap.setName(name);

        Set<CreateSectorDto> sectors = new HashSet<>();

        for (int i = 0; i < Math.random() * 10; i++) {
            sectors.add(generateRandomSector(i, (int) Math.round(Math.random() * 9), randList.get(i)));
        }

        seatmap.setSectors(sectors);
        return seatmap;
    }

    private CreateSectorDto generateRandomSector(int number, int row, int column) {
        CreateSectorDto sector = new CreateSectorDto();
        sector.setName("Sector " + number);
        sector.setPrice(BigDecimal.valueOf(1 + Math.random() * 3));
        sector.setOrientation(((int) Math.round(Math.random() * 3)) * 90);
        sector.setStandingSector(Math.random() < 0.5);
        sector.setSeatMapRow(row);
        sector.setSeatMapColumn(column);

        int type = (int) Math.round(Math.random() * 3);
        sector.setType(type == 0 ? SectorType.REGULAR :
            type == 1 ? SectorType.GRANDSTANDCURVELEFT :
                type == 2 ? SectorType.GRANDSTANDCURVERIGHT :
                    SectorType.LODGE);

        int length = 5 + (int) Math.round(Math.random() * 10);
        int width = 5 + (int) Math.round(Math.random() * 10);
        if (sector.getType() == SectorType.LODGE) {
            sector.setLodgeSize((int) Math.round(Math.random() * 4));
            length = 2 + (int) Math.round(Math.random() * 4);
            width = 2 + (int) Math.round(Math.random() * 4);
            sector.setSeats(generateLodgeSector(length, width, sector.getLodgeSize()));
        }
        if (sector.getType() == SectorType.REGULAR) {
            sector.setSeats(generateRegularSector(length, width));
        }
        if (sector.getType() == SectorType.GRANDSTANDCURVELEFT) {
            sector.setSeats(generateGrandstandCurveLeftSector(length, width));
        }
        if (sector.getType() == SectorType.GRANDSTANDCURVERIGHT) {
            sector.setSeats(generateGrandstandCurveRightSector(length, width));
        }
        sector.setLength(length);
        sector.setWidth(width);
        return sector;
    }

    private Set<CreateSeatDto> generateRegularSector(int length, int width) {
        Set<CreateSeatDto> seats = new HashSet<>();
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < width; j++) {
                seats.add(generateSeat(i * width + j + 1, i, j));
            }
        }
        return seats;
    }

    private Set<CreateSeatDto> generateGrandstandCurveLeftSector(int length, int width) {
        Set<CreateSeatDto> seats = new HashSet<>();
        int base = width;
        int number = 1;
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < base; j++) {
                seats.add(generateSeat(number, i, j));
                number++;
            }
            base++;
        }
        return seats;
    }

    private Set<CreateSeatDto> generateGrandstandCurveRightSector(int length, int width) {
        Set<CreateSeatDto> seats = new HashSet<>();
        int base = width;
        int offset = length - 1;
        int number = 1;
        for (int i = 0; i < length; i++) {
            for (int j = offset; j < offset + base; j++) {
                seats.add(generateSeat(number, i, j));
                number++;
            }
            base++;
            offset--;
        }
        return seats;
    }

    private Set<CreateSeatDto> generateLodgeSector(int length, int width, int lodgeSize) {
        Set<CreateSeatDto> seats = new HashSet<>();
        int number = 1;
        if (width % (lodgeSize + 1) != 0) {
            width = width + (lodgeSize + 1) - (width % (lodgeSize + 1));
        }
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < width; j++) {
                if (j % (lodgeSize + 1) != 0) {
                    seats.add(generateSeat(number, i, j));
                    number++;
                }
            }
        }
        return seats;
    }

    private CreateSeatDto generateSeat(int number, int row, int column) {
        CreateSeatDto seat = new CreateSeatDto(
            number,
            row,
            column
        );
        return seat;
    }

    private void mapSeatMapToSectorForCreate(Set<Sector> sectors, SeatMap seatmap) {
        LOGGER.debug("mapSeatMapToSectorForCreate({}, {})", sectors, seatmap);
        for (Sector sector : sectors) {
            sector.setSeatMap(seatmap);
            for (Seat seat : sector.getSeats()) {
                seat.setSector(sector);
            }
        }
    }
}