package at.ac.tuwien.sepm.groupphase.backend.service.impl;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CreateTicketDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedTicketDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedTicketDtoWithOrder;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper.TicketMapper;
import at.ac.tuwien.sepm.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepm.groupphase.backend.entity.Performance;
import at.ac.tuwien.sepm.groupphase.backend.entity.Seat;
import at.ac.tuwien.sepm.groupphase.backend.entity.Ticket;
import at.ac.tuwien.sepm.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepm.groupphase.backend.exception.FatalException;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepm.groupphase.backend.repository.PerformanceRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.SeatRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.TicketRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepm.groupphase.backend.service.TicketService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SimpleTicketService implements TicketService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final TicketMapper ticketMapper;

    private final TicketRepository ticketRepository;

    private final PerformanceRepository performanceRepository;

    private final SeatRepository seatRepository;

    private final UserRepository userRepository;

    private final SimpleTicketValidator ticketValidator;

    @Override
    public List<DetailedTicketDto> findAllTicketsByUser() throws BadCredentialsException {
        LOGGER.debug("Find all tickets by user");
        UUID userId = getUserId();
        if (userId == null) {
            //this should never happen
            throw new BadCredentialsException("You must be logged in to get user information");
        }
        return ticketMapper.ticketListToDetailedTicketDtoList(ticketRepository.findAllByUserWithUserPerformanceAndSeat(getUserId()));
    }

    @Override
    public DetailedTicketDto createTicket(CreateTicketDto ticketDto) throws BadCredentialsException, NotFoundException, ConflictException {
        LOGGER.debug("Create ticket");
        ticketValidator.validateTicketForCreate(ticketDto);
        Ticket ticket = ticketMapper.createTicketDtoToTicket(ticketDto);
        ticket.setPrice(calculatePrice(ticket));
        ticket.setForUser(ApplicationUser.builder().id(getUserId()).build());
        ticket.setCanceled(false);
        ticketRepository.save(ticket);
        Optional<Ticket> savedTicket = ticketRepository.findById(ticket.getId());
        if (savedTicket.isEmpty()) {
            //This should never happen
            throw new FatalException("Ticket could not be saved");
        }
        setTimedDelete(savedTicket.get());
        return ticketMapper.ticketToDetailedTicketDto(savedTicket.get());
    }


    private void setTimedDelete(Ticket ticket) {
        LOGGER.debug("setTimedDelete({})", ticket);
        // Check conditions: no orderId
        if (ticket.getOrder() == null) {
            // Start a timer for 15 minutes
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    // After 15 minutes, check conditions again
                    Ticket storedTicket = ticketRepository.findById(ticket.getId()).orElse(null);
                    if (storedTicket != null && storedTicket.getOrder() == null) {
                        LOGGER.debug("Deleting ticket with id: {}", storedTicket.getId());
                        // Delete the ticket
                        ticketRepository.deleteById(storedTicket.getId());
                    }
                }
            }, 15 * 60 * 1000); // 30 minutes in milliseconds
        }
    }

    private BigDecimal calculatePrice(Ticket ticket) {
        LOGGER.debug("calculatePrice({})", ticket);
        Performance performance = performanceRepository.findPerformanceById(ticket.getForPerformance().getId());
        Seat seat = seatRepository.findByUuId(ticket.getForSeat().getId());
        if (performance == null || seat == null) {
            //This should never happen
            throw new FatalException("Performance or seat could not be found");
        }
        return performance.getPrice().multiply(seat.getSector().getPrice());
    }

    @Override
    @Transactional
    public void deleteTicket(Long id) throws NotFoundException {
        LOGGER.debug("deleteTicket({})", id);
        long deletedRows = ticketRepository.deleteTicketById(id);
        if (deletedRows < 1) {
            throw new NotFoundException("Ticket with id: " + id + " could not be found!");
        }
        if (deletedRows > 1) {
            //This sould never happen
            throw new FatalException("There was a problem when deleting ticket with id: "
                + id + " expected one deletion, actual: " + deletedRows);
        }
    }

    @Override
    public List<DetailedTicketDtoWithOrder> findAllTicketsByPerformance(Long id) {
        LOGGER.debug("Find all tickets by performance");
        List<Ticket> tickets = ticketRepository.findAllByPerformance(id);
        return ticketMapper.ticketListToDetailedTicketDtoWithOrderList(tickets);
    }

    private UUID getUserId() throws BadCredentialsException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return null;
        }
        UUID userId = UUID.fromString(auth.getName());
        if (userRepository.findUserById(userId).isEmpty()) {
            throw new BadCredentialsException("You must be logged in to get user information");
        }
        return userId;
    }
}
