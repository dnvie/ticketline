package at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CreatePerformanceDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedPerformanceDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.SimplePerformanceDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.Performance;
import at.ac.tuwien.sepm.groupphase.backend.entity.SeatMap;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;
import java.util.UUID;


@Mapper
public interface PerformanceMapper {

    @Mapping(target = "id", ignore = true)
    Performance createPerformanceDtoToPerformance(CreatePerformanceDto createPerformanceDto);

    @Mapping(target = "seatMap", source = "seatMap.id")
    DetailedPerformanceDto performanceToDetailedPerformanceDto(Performance performance);

    List<DetailedPerformanceDto> performanceToDetailedPerformanceDto(List<Performance> performances);

    @Mapping(target = "seatMap.id", source = "seatMap")
    Performance detailedPerformanceDtoToPerformance(DetailedPerformanceDto detailedPerformanceDto);

    Set<Performance> simplePerformanceDtoToPerformance(Set<SimplePerformanceDto> performance);

    @Mapping(target = "seatMap", source = "seatMap.id")
    @Named("simplePerformance")
    SimplePerformanceDto performanceToSimplePerformanceDto(Performance performance);

    @Mapping(target = "id", source = "id")
    SeatMap map(UUID id);

}
