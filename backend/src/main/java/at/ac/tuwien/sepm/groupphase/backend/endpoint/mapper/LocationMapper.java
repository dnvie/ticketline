package at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CreateLocationDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.SimpleLocationDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.Location;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface LocationMapper {
    SimpleLocationDto locationToSimpleLocationDto(Location location);

    List<SimpleLocationDto> locationToSimpleLocationDto(List<Location> locations);

    Location simpleLocationDtoToLocation(SimpleLocationDto simpleLocationDto);

    Location createLocationDtoToLocation(CreateLocationDto createLocationDto);

    CreateLocationDto locationToCreateLocationDto(Location location);
}
