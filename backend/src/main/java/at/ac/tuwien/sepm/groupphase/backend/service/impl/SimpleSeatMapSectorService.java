package at.ac.tuwien.sepm.groupphase.backend.service.impl;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CreateSeatMapDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedSeatMapDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedSectorDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.SimpleSeatMapDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.SimpleSeatMapsWithCountDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper.SectorMapper;
import at.ac.tuwien.sepm.groupphase.backend.entity.Seat;
import at.ac.tuwien.sepm.groupphase.backend.entity.SeatMap;
import at.ac.tuwien.sepm.groupphase.backend.entity.Sector;
import at.ac.tuwien.sepm.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepm.groupphase.backend.exception.FatalException;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepm.groupphase.backend.repository.SeatMapRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.SectorRepository;
import at.ac.tuwien.sepm.groupphase.backend.service.SeatMapSectorService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SimpleSeatMapSectorService implements SeatMapSectorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final SectorRepository sectorRepository;

    private final SeatMapRepository seatmapRepository;

    private final SectorMapper sectorMapper;

    private final SimpleSeatMapValidator seatMapValidator;

    @Override
    public List<DetailedSectorDto> getAllSectors() {
        LOGGER.debug("getAllSectors()");
        return sectorMapper.sectorListToDetailedSectorDtoList(sectorRepository.findAllSectorWithSeats());
    }

    @Override
    @Transactional
    public DetailedSeatMapDto createSeatMap(CreateSeatMapDto createSeatMapDto) throws ValidationException, ConflictException {
        LOGGER.debug("createSeatMap({})", createSeatMapDto);
        seatMapValidator.validateForCreate(createSeatMapDto);
        SeatMap seatmap = sectorMapper.createSeatMapDtoToSeatMap(createSeatMapDto);
        mapSeatMapToSectorForCreate(seatmap.getSectors(), seatmap);
        seatmap = seatmapRepository.save(seatmap);
        try {
            UUID id = seatmap.getId();
            return sectorMapper.seatMapToDetailedSeatMapDto(seatmapRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Could not find seatmap with id " + id)));
        } catch (NotFoundException e) {
            //TODO: If handled in global remove this logger
            LOGGER.error("Could not find seatmap with id {}", seatmap.getId());
            throw new FatalException(String.format("Could not find seatmap with id %s", seatmap.getId()), e);
        }
    }

    @Override
    public SimpleSeatMapsWithCountDto getAllSeatMaps(int page, int size) {
        LOGGER.debug("getAllSeatMaps({},{})", page, size);
        if (size < 1) {
            return new SimpleSeatMapsWithCountDto(
                mapSeatMapListToSimpleSeatMapDtoListWithNumberOfSectorsAndSeats(
                    seatmapRepository.findAllWithPerformancesSectorsAndSeats()), seatmapRepository.countAllSeatMaps());
        }
        Pageable pageable = PageRequest.of(page, size);
        List<SeatMap> allSeatMaps = seatmapRepository.findAllWithPerformancesSectorsAndSeats(pageable);
        return new SimpleSeatMapsWithCountDto(mapSeatMapListToSimpleSeatMapDtoListWithNumberOfSectorsAndSeats(allSeatMaps), seatmapRepository.countAllSeatMaps());
    }

    private List<SimpleSeatMapDto> mapSeatMapListToSimpleSeatMapDtoListWithNumberOfSectorsAndSeats(List<SeatMap> seatMaps) {
        LOGGER.debug("mapSeatMapListToSimpleSeatMapDtoListWithNumberOfSectorsAndSeats({})", seatMaps);
        List<SimpleSeatMapDto> seatMapDtos = new ArrayList<>();
        for (SeatMap seatMap : seatMaps) {
            SimpleSeatMapDto seatMapDto = sectorMapper.seatMapToSimpleSeatMap(seatMap);
            List<Object[]> resultQuantities = seatmapRepository.getNumberOfSectorsAndSeats(seatMap.getId());
            Long numberOfSectors = (Long) resultQuantities.get(0)[0];
            Long numberOfSeats = (Long) resultQuantities.get(0)[1];


            seatMapDto.setNumberOfSeats(numberOfSeats);
            seatMapDto.setNumberOfSectors(numberOfSectors);
            seatMapDto.setIsUsed(false);
            if (seatMap.getPerformance() != null && !seatMap.getPerformance().isEmpty()) {
                seatMapDto.setIsUsed(true);
            }
            seatMapDtos.add(seatMapDto);
        }
        return seatMapDtos;
    }

    @Override
    public DetailedSeatMapDto getSeatMapById(UUID id) throws NotFoundException {
        LOGGER.debug("getSeatMapById({})", id);
        return sectorMapper.seatMapToDetailedSeatMapDto(seatmapRepository.findSeatMapByIdWithSectorAndSeats(id).orElseThrow(
            () -> new NotFoundException("Could not find seatMap with id " + id)));
    }

    @Override
    public void deleteSeatMap(UUID id) throws NotFoundException {
        LOGGER.debug("deleteSeatMap({})", id);
        SeatMap seatMap = seatmapRepository.findById(id).orElseThrow(
            () -> new NotFoundException("Could not find seatMap with id " + id));
        seatmapRepository.delete(seatMap);
    }

    @Override
    @Transactional
    public DetailedSectorDto updateSector(UUID id, DetailedSectorDto detailedSectorDto) throws NotFoundException, ValidationException, ConflictException {
        LOGGER.debug("updateSector({}, {})", id, detailedSectorDto);
        Sector toUpdate = sectorRepository.findById(id).orElseThrow(
            () -> new NotFoundException("Could not find sector with id " + id));
        Sector newValues = sectorMapper.detailedSectorDtoToSector(detailedSectorDto);
        toUpdate.setNoUpdate(newValues.getNoUpdate());
        toUpdate.setSeatMapColumn(newValues.getSeatMapColumn());
        toUpdate.setName(newValues.getName());
        toUpdate.setSeatMapRow(newValues.getSeatMapRow());
        toUpdate.setOrientation(newValues.getOrientation());
        toUpdate.setType(newValues.getType());
        toUpdate.setLodgeSize(newValues.getLodgeSize());
        toUpdate.setPrice(newValues.getPrice());
        updateSeats(toUpdate, newValues.getSeats());
        sectorRepository.save(toUpdate);
        return sectorMapper.sectorToDetailedSectorDto(toUpdate);
    }

    private void updateSectorsInSeatmap(SeatMap toUpdate, Set<Sector> newValues) {
        LOGGER.debug("updateSector({}, {})", toUpdate, newValues);
        Set<Sector> oldValues = toUpdate.getSectors();
        oldValues.clear();
        for (Sector sector : newValues) {
            sector.setSeatMap(toUpdate);
            for (Seat seat : sector.getSeats()) {
                seat.setSector(sector);
            }
        }
        oldValues.addAll(newValues);
    }

    @Override
    public DetailedSectorDto getSectorById(UUID id) throws NotFoundException {
        LOGGER.debug("getSectorById({})", id);
        return sectorMapper.sectorToDetailedSectorDto(sectorRepository.findSectorWithSeatsById(id).orElseThrow(
            () -> new NotFoundException("Could not find sector with id " + id)));
    }

    @Override
    @Transactional
    public DetailedSeatMapDto updateSeatMap(UUID id, DetailedSeatMapDto newValuesDto) throws NotFoundException {
        LOGGER.debug("updateSeatMap({}, {})", id, newValuesDto);
        SeatMap toUpdate = seatmapRepository.findSeatMapByIdWithSectorAndSeats(id).orElseThrow(
            () -> new NotFoundException("Could not find seatmap with id " + id));
        SeatMap newValues = sectorMapper.detailedSeatMapDtoToSeatMap(newValuesDto);
        toUpdate.setName(newValues.getName());
        updateSectorsInSeatmap(toUpdate, newValues.getSectors());
        seatmapRepository.save(toUpdate);
        return sectorMapper.seatMapToDetailedSeatMapDto(toUpdate);
    }

    private void updateSeats(Sector toUpdate, Set<Seat> updatedSet) {
        LOGGER.debug("updateSeats({})", toUpdate);
        Set<Seat> seats = toUpdate.getSeats();
        seats.clear();
        seats.addAll(updatedSet);
        for (Seat seat : seats) {
            seat.setSector(toUpdate);
        }
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
