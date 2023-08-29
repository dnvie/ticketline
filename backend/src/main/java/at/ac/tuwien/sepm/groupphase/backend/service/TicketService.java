package at.ac.tuwien.sepm.groupphase.backend.service;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CreateTicketDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedTicketDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedTicketDtoWithOrder;
import at.ac.tuwien.sepm.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.List;

public interface TicketService {


    /**
     * Find all tickets of a User.
     *
     * @return the found ticket entries
     * @throws BadCredentialsException when User not found
     */
    List<DetailedTicketDto> findAllTicketsByUser() throws BadCredentialsException;

    /**
     * Create a new ticket entry.
     *
     * @param ticketDto the ticket entry to create
     * @return the created ticket entry
     * @throws ConflictException       when ticketDto is already in use
     * @throws NotFoundException       when Performance or Seat not found
     * @throws BadCredentialsException when User not logged in correctly
     */
    DetailedTicketDto createTicket(CreateTicketDto ticketDto) throws BadCredentialsException, NotFoundException, ConflictException;

    /**
     * Delete a ticket entry.
     *
     * @param id the id of the ticket to delete
     * @throws NotFoundException when ticket with id could not be found
     */
    void deleteTicket(Long id) throws NotFoundException;

    /**
     * Find all tickets of a Performance that have not been canceled.
     *
     * @return the found ticket entries
     * @throws NotFoundException when Performance not found
     */
    List<DetailedTicketDtoWithOrder> findAllTicketsByPerformance(Long id) throws NotFoundException;

}
