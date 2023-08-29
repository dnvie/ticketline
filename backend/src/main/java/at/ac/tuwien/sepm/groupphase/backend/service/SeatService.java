package at.ac.tuwien.sepm.groupphase.backend.service;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedCreateSeatDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedSeatDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.SimpleSeatDto;
import at.ac.tuwien.sepm.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.exception.ValidationException;

import java.util.List;
import java.util.UUID;

public interface SeatService {

    /**
     * Gets all seats from the database.
     *
     * @return list of all seats
     */
    List<SimpleSeatDto> getAllSeats();

    /**
     * Gets a seat by its id.
     *
     * @param id of the seat to find
     * @return the seat with the specified id
     * @throws NotFoundException if the seat could not be found
     */

    SimpleSeatDto getSeatById(UUID id) throws NotFoundException;

    /*    /**
     * Reserves a seat by its id.
     *
     * @param id of the seat to reserve
     * @return the seat with the specified id
     * @throws NotFoundException if the seat could not be found
     * @throws ConflictException if the seat is already reserved
     *//*
    SimpleSeatDto reserveSeat(UUID id) throws NotFoundException, ConflictException;*/

    /**
     * Updates a seat.
     *
     * @param seatDto the seat to update
     * @param id      of the seat to update
     * @return the updated seat
     * @throws NotFoundException if the seat could not be found
     * @throws ConflictException if the seat is already reserved or booked
     */
    SimpleSeatDto updateSeat(SimpleSeatDto seatDto, UUID id) throws NotFoundException, ConflictException;

    /**
     * Creates a seat.
     *
     * @param seatDto the seat to create
     * @return the created seat
     * @throws NotFoundException   if the seat could not be found
     * @throws ValidationException if the seat is invalid
     * @throws ConflictException   if the seat has no unique number in the sector
     */
    DetailedSeatDto createSeat(DetailedCreateSeatDto seatDto) throws NotFoundException, ValidationException, ConflictException;

    /*    *//*
     * Deletes a seat by its id.
     *
     * @param id of the seat to delete
     * @return the deleted seat
     *//*
    SimpleSeatDto unreserveSeat(UUID id);*/
}
