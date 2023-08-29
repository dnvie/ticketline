package at.ac.tuwien.sepm.groupphase.backend.datagenerator;

import at.ac.tuwien.sepm.groupphase.backend.entity.Location;
import at.ac.tuwien.sepm.groupphase.backend.entity.Seat;
import at.ac.tuwien.sepm.groupphase.backend.entity.SeatMap;
import at.ac.tuwien.sepm.groupphase.backend.entity.Sector;
import at.ac.tuwien.sepm.groupphase.backend.entity.enums.SectorType;
import at.ac.tuwien.sepm.groupphase.backend.repository.LocationRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.SeatRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.SeatMapRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.SectorRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Profile("generateData")
@Component
public class LocationDataGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final int NUMBER_OF_SEATMAPS_TO_GENERATE = 3;

    private final LocationRepository locationRepository;
    private final SeatMapRepository seatmapRepository;

    private final SectorRepository sectorRepository;
    private final SeatRepository seatRepository;

    private static final Random r = new Random();


    public LocationDataGenerator(LocationRepository locationRepository, SeatMapRepository seatmapRepository,
                                 SectorRepository sectorRepository, SeatRepository seatRepository) {
        this.locationRepository = locationRepository;
        this.seatmapRepository = seatmapRepository;
        this.sectorRepository = sectorRepository;
        this.seatRepository = seatRepository;
    }

    public void generateLocation() {
        if (locationRepository.findAll().size() > 1) {
            LOGGER.debug("location already generated");
        } else {
            LOGGER.debug("generating location entries");
            List<Location> locationList = new ArrayList<>();
            for (int i = 0; i < 125; i++) {
                locationList.add(Location.generateRandomLocation());
            }
            locationRepository.saveAll(locationList);
        }
    }

    private void generateSeatmap() {
        if (!seatmapRepository.findAll().isEmpty()) {
            LOGGER.debug("seatmap already generated");
        } else {
            LOGGER.debug("generating {} seatmap entries", NUMBER_OF_SEATMAPS_TO_GENERATE);
            for (int i = 0; i < NUMBER_OF_SEATMAPS_TO_GENERATE; i++) {
                SeatMap seatmap = SeatMap.builder()
                    .name("Seatmap " + i)
                    .build();
                LOGGER.debug("saving seatmap {}", seatmap);
                seatmapRepository.save(seatmap);
                generateSectors(r.nextInt(4), seatmap);
            }
        }
    }

    private void generateSectors(int numberOfSectors, SeatMap seatmap) {
        LOGGER.debug("generating {} sector entries", numberOfSectors);
        for (int i = 0; i < numberOfSectors; i++) {
            Sector sector = Sector.builder()
                .name("Sector " + i)
                .seatMap(seatmap)
                .seatMapColumn(i)
                .seatMapRow(i)
                .orientation(r.nextInt(360))
                .price(BigDecimal.valueOf(1 + (r.nextDouble() * (2 - 1))))
                .type(SectorType.values()[r.nextInt(SectorType.values().length)])
                .noUpdate(false)
                .length(r.nextInt(5))
                .width(r.nextInt(5))
                .build();
            if (sector.getType() == SectorType.LODGE) {
                sector.setLodgeSize(r.nextInt() * 5);
            }
            LOGGER.debug("saving sector {}", sector);
            sectorRepository.save(sector);
            generateSeats(sector, r.nextInt(15));
        }
    }

    //TODO: overthink seat generation
    private void generateSeats(Sector sector, int numberOfSeats) {
        LOGGER.debug("generating {} seat entries", numberOfSeats);
        for (int i = 0; i < numberOfSeats; i++) {
            Seat seat = Seat.builder()
                .sector(sector)
                .number(i)
                //.reserved(null)
                .seatColumn(i)
                .seatRow(i)
                .build();
            LOGGER.debug("saving seat {}", seat);
            seatRepository.save(seat);
        }
    }
}
