package at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CreateSeatMapDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CreateSectorDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedSeatMapDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedSectorDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.SimpleSeatDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.SimpleSeatMapDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.SimpleSectorDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.Seat;
import at.ac.tuwien.sepm.groupphase.backend.entity.SeatMap;
import at.ac.tuwien.sepm.groupphase.backend.entity.Sector;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;

@Mapper
public interface SectorMapper {

    Sector simpleSectorDtoToSector(SimpleSectorDto simpleSectorDto);

    @Named("simpleSector")
    SimpleSectorDto sectorToSectorDto(Sector sector);

    /*
        @Mapping(target = "reservedFor", source = "reserved.id")
    */
    @Mapping(target = "sector", source = "sector.name")
    SimpleSeatDto seatToSimpleSeatDto(Seat seat);

    @Named("detailedSector")
    DetailedSectorDto sectorToDetailedSectorDto(Sector sector);

    Sector detailedSectorDtoToSector(DetailedSectorDto detailedSectorDto);

    List<Sector> sectorDtoListToSectorList(List<SimpleSectorDto> sectorDtoList);

    List<SimpleSectorDto> sectorListToSectorDtoList(List<Sector> sectorList);

    List<DetailedSectorDto> sectorListToDetailedSectorDtoList(List<Sector> sectorList);

    List<Sector> detailedSectorDtoListToSectorList(List<DetailedSectorDto> detailedSectorDtoList);

    CreateSeatMapDto seatMapToCreateSeatMapDto(SeatMap seatmap);

    SeatMap createSeatMapDtoToSeatMap(CreateSeatMapDto createSeatMapDto);

    List<SimpleSeatMapDto> seatMapListToSimpleSeatMapDtoList(List<SeatMap> seatMapList);

    List<SeatMap> simpleSeatMapDtoListToSeatMapList(List<SimpleSeatMapDto> simpleSeatMapDtoList);

    @Named("createSector")
    CreateSectorDto sectorToCreateSectorDto(Sector sector);

    Set<CreateSectorDto> sectorSetToCreateSectorDtoSet(Set<Sector> sectorSet);

    Set<Sector> createSectorDtoSetToSectorSet(Set<CreateSectorDto> createSectorDtoSet);

    Sector createSectorDtoToSector(CreateSectorDto createSectorDto);

    @Named("simpleSeatMap")
    SimpleSeatMapDto seatMapToSimpleSeatMap(SeatMap seatmap);

    SeatMap simpleSeatMapDtoToSeatMap(SimpleSeatMapDto simpleSeatMapDto);

    @Named("detailedSeatMap")
    @Mapping(target = "numberOfSeats", ignore = true)
    @Mapping(target = "numberOfSectors", ignore = true)
    DetailedSeatMapDto seatMapToDetailedSeatMapDto(SeatMap seatMap);

    SeatMap detailedSeatMapDtoToSeatMap(DetailedSeatMapDto detailedSeatMapDto);

    @Mapping(target = "sector", ignore = true)
    Seat simpleSeatDtoToSeat(SimpleSeatDto simpleSeatDto);
}
