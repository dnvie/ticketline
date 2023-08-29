package at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CreateOrderDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedOrderDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedTicketDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.Order;
import at.ac.tuwien.sepm.groupphase.backend.entity.Performance;
import at.ac.tuwien.sepm.groupphase.backend.entity.Seat;
import at.ac.tuwien.sepm.groupphase.backend.entity.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.UUID;

@Mapper
public interface OrderMapper {
    @Mapping(target = "id", ignore = true)
    Order createOrderDtoToOrder(CreateOrderDto createOrderDto);

    @Mapping(target = "id", source = "id")
    Performance idToPerformance(Long id);

    @Mapping(target = "id", source = "id")
    Seat idToSeat(UUID id);

    @Mapping(target = "forPerformance.seatMap.id", source = "forPerformance.seatMap")
    Ticket detailedTicketDtoToTicket(DetailedTicketDto detailedTicketDto);

    @Mapping(target = "forPerformance.seatMap", source = "forPerformance.seatMap.id")
    DetailedTicketDto ticketToDetailedTicketDto(Ticket ticket);

    Order detailedOrderDtoToOrder(DetailedOrderDto detailedOrderDto);

    DetailedOrderDto orderToDetailedOrderDto(Order order);

    List<DetailedOrderDto> orderToDetailedOrderDto(List<Order> orders);

}
