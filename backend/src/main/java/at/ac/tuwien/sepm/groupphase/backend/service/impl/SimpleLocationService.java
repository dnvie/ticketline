package at.ac.tuwien.sepm.groupphase.backend.service.impl;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CreateLocationDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.SimpleLocationDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper.LocationMapper;
import at.ac.tuwien.sepm.groupphase.backend.entity.Location;
import at.ac.tuwien.sepm.groupphase.backend.exception.FatalException;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.repository.LocationRepository;
import at.ac.tuwien.sepm.groupphase.backend.service.LocationService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Optional;

@Service
public class SimpleLocationService implements LocationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;

    public SimpleLocationService(LocationRepository locationRepository, LocationMapper locationMapper) {
        this.locationRepository = locationRepository;
        this.locationMapper = locationMapper;
    }

    @Override
    public List<SimpleLocationDto> findAll(int page, int size) {
        LOGGER.debug("Find all locations");
        if (size < 1) {
            return this.locationMapper.locationToSimpleLocationDto(locationRepository.findAll());
        }
        Pageable pageable = PageRequest.of(page, size);
        return this.locationMapper.locationToSimpleLocationDto(locationRepository.findAll(pageable).getContent());
    }

    @Override
    public SimpleLocationDto findOne(Long id) throws NotFoundException {
        LOGGER.debug("Find location with id {}", id);
        Location location = locationRepository.findById(id).orElseThrow(
            () -> new NotFoundException(String.format("Could not find location with id %d", id)));
        return this.locationMapper.locationToSimpleLocationDto(location);
    }

    @Override
    public SimpleLocationDto createLocation(CreateLocationDto createLocationDto) {
        LOGGER.debug("Create location {}", createLocationDto);
        Location location = this.locationMapper.createLocationDtoToLocation(createLocationDto);
        location = locationRepository.save(location);
        return this.locationMapper.locationToSimpleLocationDto(location);
    }

    @Override
    public SimpleLocationDto updateLocation(Long id, SimpleLocationDto simpleLocationDto) throws NotFoundException {
        LOGGER.debug("Update location {}", simpleLocationDto);
        Optional<Location> toUpdate = locationRepository.findById(id);
        if (toUpdate.isEmpty()) {
            throw new NotFoundException(String.format("Could not find location with id %d", id));
        }
        Location location = toUpdate.get();
        location.setName(simpleLocationDto.getName());
        location.setStreet(simpleLocationDto.getStreet());
        location.setCity(simpleLocationDto.getCity());
        location.setCountry(simpleLocationDto.getCountry());
        location.setPostalCode(simpleLocationDto.getPostalCode());
        location.setDescription(simpleLocationDto.getDescription());

        Location updatedLocation = locationRepository.save(location);
        return this.locationMapper.locationToSimpleLocationDto(updatedLocation);
    }

    @Override
    @Transactional
    public void deleteLocation(Long id) throws NotFoundException {
        LOGGER.debug("deleteLocation({})", id);
        Long deletedCount = locationRepository.deleteLocationById(id);
        if (deletedCount < 1) {
            throw new NotFoundException(String.format("Could not find location with id %d", id));
        } else if (deletedCount > 1) {
            //TODO: If handled in global remove this logger
            LOGGER.error("Expected 1 event to be deleted, but {} were deleted", deletedCount);
            throw new FatalException(String.format("Expected 1 location to be deleted, but %d were deleted", deletedCount));
        }

    }
}
