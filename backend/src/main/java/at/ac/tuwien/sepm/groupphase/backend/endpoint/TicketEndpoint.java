package at.ac.tuwien.sepm.groupphase.backend.endpoint;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CreateTicketDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedTicketDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedTicketDtoWithOrder;
import at.ac.tuwien.sepm.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.service.TicketService;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.lang.invoke.MethodHandles;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/tickets")
@RequiredArgsConstructor
public class TicketEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final TicketService ticketService;

    @GetMapping
    @Secured("ROLE_USER")
    public List<DetailedTicketDto> getTickets() throws BadCredentialsException {
        LOGGER.info("GET /api/v1/tickets");
        return ticketService.findAllTicketsByUser();
    }

    @GetMapping(value = "/performance/{id}")
    @PermitAll
    public List<DetailedTicketDtoWithOrder> getTicketByPerformance(@PathVariable Long id) throws NotFoundException {
        LOGGER.info("GET /api/v1/tickets/performance/{}", id);
        return ticketService.findAllTicketsByPerformance(id);
    }

    @PostMapping
    @PermitAll
    @ResponseStatus(HttpStatus.CREATED)
    public DetailedTicketDto createTicket(@Valid @RequestBody CreateTicketDto ticketDto)
        throws BadCredentialsException, NotFoundException, ConflictException {
        LOGGER.info("POST /api/v1/tickets");
        return ticketService.createTicket(ticketDto);
    }

    @DeleteMapping(value = "/{id}")
    @PermitAll
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTicket(@PathVariable Long id) throws NotFoundException {
        LOGGER.info("DELETE /api/v1/tickets/{}", id);
        ticketService.deleteTicket(id);
    }

}
