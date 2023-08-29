package at.ac.tuwien.sepm.groupphase.backend.service;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CreateSeatMapDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedSeatMapDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedSectorDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.SimpleSeatMapDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.SimpleSeatMapsWithCountDto;
import at.ac.tuwien.sepm.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.exception.ValidationException;

import java.util.List;
import java.util.UUID;

//TODO: maybe split it into two service implementations or two services or two endpoints
public interface SeatMapSectorService {

    /**
     * Gets all sectors.
     *
     * @return list of all sectors
     */
    List<DetailedSectorDto> getAllSectors();

    /**
     * Creates a new seat map.
     *
     * @param createSeatMapDto the seat map to be created
     * @return the created seat map
     * @throws ValidationException if the seat map is invalid
     * @throws ConflictException   if the seat map already exists
     */
    DetailedSeatMapDto createSeatMap(CreateSeatMapDto createSeatMapDto) throws ValidationException, ConflictException;

    /**
     * Gets all seat maps with pagination.
     *
     * @param page the page number
     * @param size the page size
     * @return list of all seat maps
     */
    SimpleSeatMapsWithCountDto getAllSeatMaps(int page, int size);

    /**
     * Gets a seat map by id.
     *
     * @param id the id of the seat map
     * @return the seat map
     * @throws NotFoundException if the seat map does not exist
     */
    DetailedSeatMapDto getSeatMapById(UUID id) throws NotFoundException;

    /**
     * Deletes a seat map by id.
     *
     * @param id the id of the seat map
     * @throws NotFoundException if the seat map does not exist
     */
    void deleteSeatMap(UUID id) throws NotFoundException;

    /**
     * Updates a sector.
     *
     * @param id                of the sector to update
     * @param detailedSectorDto the updated sector
     * @return the updated sector
     * @throws NotFoundException   if the sector does not exist
     * @throws ValidationException if the sector is invalid
     * @throws ConflictException   if the sector cannot be updated
     */
    DetailedSectorDto updateSector(UUID id, DetailedSectorDto detailedSectorDto) throws NotFoundException, ValidationException, ConflictException;

    /**
     * Gets a sector by id.
     *
     * @param id the id of the sector
     * @throws NotFoundException if the sector does not exist
     */
    DetailedSectorDto getSectorById(UUID id) throws NotFoundException;

    /**
     * Updates a seat map.
     *
     * @param id               of the seat map to update
     * @param createSeatMapDto the updated seat map
     * @return the updated seat map
     * @throws NotFoundException if the seat map does not exist
     */
    DetailedSeatMapDto updateSeatMap(UUID id, DetailedSeatMapDto createSeatMapDto) throws NotFoundException;
}
