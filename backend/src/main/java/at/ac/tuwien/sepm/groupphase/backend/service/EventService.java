package at.ac.tuwien.sepm.groupphase.backend.service;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedEventDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.SearchDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.SearchResultsDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CreateEventDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.TopTenEventDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.UpdateEventDto;
import at.ac.tuwien.sepm.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.exception.ValidationException;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EventService {
    /**
     * Find all event entries.
     */
    List<DetailedEventDto> findAll(Pageable pageable);

    /**
     * Find all performer, locations and events, that match with the given search parameters.
     *
     * @param searchDto the search values
     * @return the matching entities
     */
    SearchResultsDto search(SearchDto searchDto);

    /**
     * Create a new event entry.
     *
     * @param createEventDto the event entry, could contain performances, to create
     * @return the created event entry
     * @throws ValidationException if the createDto entry is not valid
     * @throws ConflictException   if the event entry is in conflict with the data currently in the system
     */
    DetailedEventDto createEvent(CreateEventDto createEventDto) throws ValidationException, ConflictException;

    /**
     * Delete an event entry.
     *
     * @param id the id of the event entry to delete
     * @throws NotFoundException if the event entry could not be found
     * @throws ConflictException if the event entry could not be deleted because of conflicts
     */
    void deleteEvent(Long id) throws NotFoundException, ConflictException;

    /**
     * Update an event entry.
     *
     * @param id             the id of the event entry to update
     * @param updateEventDto the event entry, could contain performances, to update
     * @return the updated event entry
     * @throws NotFoundException   if the event entry could not be found
     * @throws ValidationException if the updateDto is not valid
     * @throws ConflictException   if the event entry is in conflict with the data currently in the system
     */
    DetailedEventDto updateEvent(Long id, UpdateEventDto updateEventDto) throws NotFoundException, ValidationException, ConflictException;


    /**
     * Find one event entry by id.
     *
     * @param id the id of the event entry
     * @return the event entry
     * @throws NotFoundException if the event entry could not be found
     */
    DetailedEventDto findOne(Long id) throws NotFoundException;

    /**
     * Find all event entries where a certain performer performs.
     *
     * @param name the name of the performer
     * @return the event entries
     * @throws NotFoundException if the performer does not perform at any event
     */
    List<DetailedEventDto> findEventsByPerformerName(String name) throws NotFoundException;

    /**
     * Find all event entries where at least one performance is at a given location.
     *
     * @param name the name of the location
     * @return the event entries
     * @throws NotFoundException if the event has no performance at the given location
     */
    List<DetailedEventDto> findEventsByLocation(String name) throws NotFoundException;


    /**
     * Returns the top ten events of a given month and year by ticket sales.
     *
     * @param year the specified year of the events
     * @param month the specified month of the events
     * @param type the type of the events, null if all types should be considered
     * @return the top ten events of the given month and year
     */
    List<TopTenEventDto> getTopTenEvents(int year, int month, String type);

    Long getTotalEventCount();
}
