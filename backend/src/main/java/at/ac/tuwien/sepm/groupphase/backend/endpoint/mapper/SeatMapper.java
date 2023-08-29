package at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CreateSeatDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedCreateSeatDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedSeatDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.SimpleSeatDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.Seat;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;

@Mapper
public interface SeatMapper {

    @Mapping(target = "sector", ignore = true)
    Seat seatDtoToSeat(SimpleSeatDto seatDto);

    @Mapping(target = "sector", ignore = true)
    SimpleSeatDto seatToSimpleSeatDto(Seat seat);

    @Named("detailedSeat")
    DetailedSeatDto seatToDetailedSeatDto(Seat seat);

    Seat detailedSeatDtoToSeat(DetailedSeatDto detailedSeatDto);


    List<Seat> seatDtoListToSeatList(List<SimpleSeatDto> seatDtoList);

    List<DetailedSeatDto> seatListToDetailedSeatDtoList(List<Seat> seatList);


    List<Seat> detailedSeatDtoListToSeatList(List<DetailedSeatDto> detailedSeatDtoList);

    List<SimpleSeatDto> seatListToSeatDtoList(List<Seat> seatList);

    Set<Seat> simpleSeatSetDtoToSeatSet(Set<SimpleSeatDto> seatDtoSet);

    Set<SimpleSeatDto> seatSetToSimpleSeatSetDto(Set<Seat> seatSet);

    @Named("createSeat")
    CreateSeatDto seatToCreateSeatDto(Seat seat);

    @Named("detailedCreateSeat")
    DetailedCreateSeatDto seatToDetailedCreateSeatDto(Seat seat);

    Seat detailedCreateSeatDtoToSeat(DetailedCreateSeatDto detailedCreateSeatDto);

    Set<CreateSeatDto> seatSetToCreateSeatDtoSet(Set<Seat> seatSet);

    Set<Seat> createSeatDtoSetToSeatSet(Set<CreateSeatDto> createSeatDtoSet);

    Seat createSeatDtoToSeat(CreateSeatDto createSeatDto);

}
