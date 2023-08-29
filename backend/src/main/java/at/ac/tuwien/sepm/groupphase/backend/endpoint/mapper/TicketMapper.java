package at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CreateTicketDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedTicketDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedTicketDtoWithOrder;
import at.ac.tuwien.sepm.groupphase.backend.entity.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;

@Mapper
public interface TicketMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "forPerformance.id", source = "forPerformance")
    @Mapping(target = "forSeat.id", source = "forSeat")
    Ticket createTicketDtoToTicket(CreateTicketDto createTicketDto);

    @Mapping(target = "forPerformance.seatMap.id", source = "forPerformance.seatMap")
    Ticket detailedTicketDtoToTicket(DetailedTicketDto detailedTicketDto);

    @Mapping(target = "forPerformance.seatMap.id", source = "forPerformance.seatMap")
    List<Ticket> detailedTicketDtoListToTicketList(List<DetailedTicketDto> detailedTicketDto);

    @Mapping(target = "forPerformance.seatMap", source = "forPerformance.seatMap.id")
    @Mapping(target = "seat.id", source = "forSeat.id")
    @Mapping(target = "seat.sector", source = "forSeat.sector.name")
    @Mapping(target = "seat.standingSector", source = "forSeat.sector.standingSector")
    DetailedTicketDto ticketToDetailedTicketDto(Ticket ticket);

    @Mapping(target = "forPerformance.seatMap", source = "forPerformance.seatMap.id")
    @Mapping(target = "seat.id", source = "forSeat.id")
    @Mapping(target = "seat.sector", source = "forSeat.sector.name")
    @Mapping(target = "order", source = "order.id")
    DetailedTicketDtoWithOrder ticketToDetailedTicketDtoWithOrder(Ticket ticket);

    Set<DetailedTicketDto> ticketSetToDetailedTicketDtoSet(Set<Ticket> ticketSet);

    List<DetailedTicketDto> ticketListToDetailedTicketDtoList(List<Ticket> ticketList);

    List<DetailedTicketDtoWithOrder> ticketListToDetailedTicketDtoWithOrderList(List<Ticket> ticketList);
}
