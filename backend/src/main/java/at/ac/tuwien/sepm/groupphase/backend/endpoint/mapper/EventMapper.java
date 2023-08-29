package at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CreateEventDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CreatePerformanceDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedEventDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedPerformanceDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.SimpleEventDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.SimplePerformanceDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.Event;
import at.ac.tuwien.sepm.groupphase.backend.entity.Performance;
import at.ac.tuwien.sepm.groupphase.backend.entity.SeatMap;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.UUID;

@Mapper
public interface EventMapper {

    @Named("simpleEvent")
    SimpleEventDto eventToSimpleEventDto(Event event);

    Event simpleEventDtoToEvent(SimpleEventDto simpleEventDto);

    List<Event> simpleEventDtoToEvent(List<SimpleEventDto> simpleEventDto);

    @Mapping(target = "id", ignore = true)
    Event createEventDtoToEvent(CreateEventDto createEventDto);

    //Needed for createEventDtoToEvent to ignore the event property of Performance
    @Mapping(target = "event", ignore = true)
    Performance createPerformanceDtoToPerformance(CreatePerformanceDto createPerformanceDto);

    @Named("detailedEvent")
    DetailedEventDto eventToDetailedEventDto(Event event);

    Event detailedEventDtoToEvent(DetailedEventDto detailedEventDto);

    List<DetailedEventDto> eventsToDetailedEventDtos(List<Event> event);

    @Mapping(target = "seatMap", source = "seatMap.id")
    SimplePerformanceDto performanceToSimplePerformanceDto(Performance performance);

    @Mapping(target = "seatMap.id", source = "seatMap")
    Performance detailedPerformanceDtoToPerformance(DetailedPerformanceDto detailedPerformanceDto);

    @Mapping(target = "id", source = "id")
    SeatMap map(UUID id);
}
