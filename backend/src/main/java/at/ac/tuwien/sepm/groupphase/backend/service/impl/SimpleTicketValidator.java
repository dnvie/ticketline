package at.ac.tuwien.sepm.groupphase.backend.service.impl;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CreateTicketDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.SimplePerformanceDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.SimpleSeatDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper.PerformanceMapper;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper.SeatMapper;
import at.ac.tuwien.sepm.groupphase.backend.entity.Ticket;
import at.ac.tuwien.sepm.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepm.groupphase.backend.repository.PerformanceRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.SeatRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SimpleTicketValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final TicketRepository ticketRepository;

    private final SeatRepository seatRepository;
    private final SeatMapper seatMapper;

    private final PerformanceRepository performanceRepository;
    private final PerformanceMapper performanceMapper;


    public void validateTicketForCreate(CreateTicketDto ticketDto) throws NotFoundException, ConflictException {
        LOGGER.debug("Validate ticket");
        List<String> conflictErrors = new ArrayList<>();
        SimpleSeatDto seat = seatMapper.seatToSimpleSeatDto(seatRepository.findByUuId(ticketDto.getForSeat()));
        if (seat == null) {
            throw new NotFoundException("Seat not found");
        }
        SimplePerformanceDto performance = performanceMapper.performanceToSimplePerformanceDto(performanceRepository.findPerformanceById(ticketDto.getForPerformance()));
        if (performance == null) {
            //TODO: maybe throw notFoundException
            conflictErrors.add("Performance does not exist");
        } else {
            List<Ticket> ticket = ticketRepository.findTicketByPerformanceAndSeat(performance.getId(), seat.getId());
            if (ticket != null && !ticket.isEmpty() && ticket.stream().filter(Ticket::isCanceled).count() < ticket.size()) {
                conflictErrors.add("Ticket already exists");
            }
        }

        if (!conflictErrors.isEmpty()) {
            throw new ConflictException("Creation of ticket failed: ", conflictErrors);
        }
    }
}
