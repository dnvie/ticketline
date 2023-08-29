package at.ac.tuwien.sepm.groupphase.backend.service;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CreatePerformanceDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedPerformanceDto;
import at.ac.tuwien.sepm.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.exception.ValidationException;

import java.util.List;


public interface PerformanceService {
    /**
     * Create a single performance entry.
     *
     * @param performance to create
     * @return newly created performance entry
     * @throws ValidationException when the dto to create has invalid data
     * @throws ConflictException   when the given data is in conflict with the persistent data
     */
    DetailedPerformanceDto createPerformance(CreatePerformanceDto performance) throws ValidationException, ConflictException;

    /**
     * Find all performance entries.
     *
     * @return list of all performance entries
     */
    List<DetailedPerformanceDto> findAll();

    /**
     * Find the performance entry with this specific id.
     *
     * @param id of the performance which should be found
     * @return the performance entry with this specific id
     * @throws NotFoundException if no performance with this id is found
     */

    DetailedPerformanceDto findById(Long id);

    /**
     * delete the performance entry with this specific id.
     *
     * @param id of the performance which should be deleted
     * @throws NotFoundException if no performance with this id is found to delete
     */
    void deleteById(Long id);

    /**
     * Update a performance entry.
     *
     * @param id                     the id of the performance entry to update
     * @param detailedPerformanceDto the performance entry to update
     * @return the updated performance entry
     * @throws NotFoundException   if the performance entry could not be found
     * @throws ConflictException   when the given data is in conflict with the persistent data
     * @throws ValidationException when the dto to create has invalid data
     */
    DetailedPerformanceDto updatePerformance(Long id, DetailedPerformanceDto detailedPerformanceDto)
        throws NotFoundException, ValidationException, ConflictException;
}
