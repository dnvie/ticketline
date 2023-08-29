package at.ac.tuwien.sepm.groupphase.backend.service;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CreateLocationDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.SimpleLocationDto;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;

import java.util.List;

public interface LocationService {
    /**
     * Find all location entries if size > 0 and page >= 0, otherwise find all location entries.
     *
     * @param page the page number
     * @param size the page size
     * @return list of all location entries
     */
    List<SimpleLocationDto> findAll(int page, int size);

    /**
     * Find a location entry with a certain id.
     *
     * @param id of the location entry to find
     * @return location entry matching the given id
     * @throws NotFoundException will be thrown if no location entry could be found with the given id
     */
    SimpleLocationDto findOne(Long id) throws NotFoundException;

    /**
     * Create a new location entry.
     *
     * @param createLocationDto the location entry to be created
     * @return the location entry that has been created
     */
    SimpleLocationDto createLocation(CreateLocationDto createLocationDto);

    /**
     * Update a location entry.
     *
     * @param id                of the location entry to be updated
     * @param simpleLocationDto the location entry to be updated
     * @return the location entry that has been updated
     * @throws NotFoundException will be thrown if no location entry could be found with the given id
     */
    SimpleLocationDto updateLocation(Long id, SimpleLocationDto simpleLocationDto) throws NotFoundException;

    /**
     * Delete a location entry.
     *
     * @param id of the location entry to be deleted
     * @throws NotFoundException will be thrown if no location entry could be found with the given id
     */
    void deleteLocation(Long id) throws NotFoundException;
}
