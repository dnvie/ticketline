package at.ac.tuwien.sepm.groupphase.backend.service.impl;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CreateOrderDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedOrderDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedTicketDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepm.groupphase.backend.entity.enums.OrderType;
import at.ac.tuwien.sepm.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepm.groupphase.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SimpleOrderValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final UserRepository userRepository;

    public void validateOrderForCreate(CreateOrderDto createOrderDto, UUID user) throws ValidationException {
        LOGGER.debug("Validate order for create");
        List<ApplicationUser> forUsers = this.userRepository.findUserById(user);
        if (forUsers.isEmpty()) {
            throw new NotFoundException(String.format("Could not find user with uuid %s", user));
        }
        List<String> validationErrors = new ArrayList<>();
        if (createOrderDto.getTickets().size() == 0) {
            validationErrors.add("cannot create a order without tickets");
        }
        for (DetailedTicketDto t : createOrderDto.getTickets()) {
            if (t.getForPerformance().getStartTime().isBefore(LocalDateTime.now())) {
                validationErrors.add("cannot buy tickets for performances that already started");
                break;
            }
        }
        if (createOrderDto.getOrderType() != OrderType.BUY && createOrderDto.getOrderType() != OrderType.RESERVATION
            && createOrderDto.getOrderType() != OrderType.STORNO) {
            validationErrors.add("order type is not valid");
        }
        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation of order for create failed ", validationErrors);
        }
    }

    public void validateOrderForBuyingTicketsOfReservation(DetailedOrderDto detailedOrderDto, List<DetailedTicketDto> ticketsToBuy) throws NotFoundException, ConflictException {
        LOGGER.debug("Validate order for buying tickets of order");
        List<ApplicationUser> forUsers = this.userRepository.findUserById(detailedOrderDto.getOrderBy().getId());
        if (forUsers.isEmpty()) {
            throw new NotFoundException(String.format("Could not find user with uuid %s", detailedOrderDto.getOrderBy().getId()));
        }
        List<String> conflictErrors = new ArrayList<>();
        if (ticketsToBuy.isEmpty()) {
            conflictErrors.add("no tickets to buy found!");
        }
        if (!conflictErrors.isEmpty()) {
            throw new ConflictException("buying tickets of order failed because of conflicts", conflictErrors);
        }
    }

    public void validateOrderForCancellation(DetailedOrderDto detailedOrderDto, List<DetailedTicketDto> ticketsToCancel) throws NotFoundException, ConflictException, ValidationException {
        LOGGER.debug("Validate order for cancel tickets of order");
        List<ApplicationUser> forUsers = this.userRepository.findUserById(detailedOrderDto.getOrderBy().getId());
        if (forUsers.isEmpty()) {
            throw new NotFoundException(String.format("Could not find user with uuid %s", detailedOrderDto.getOrderBy().getId()));
        }
        List<String> validationErrors = new ArrayList<>();
        List<String> conflictErrors = new ArrayList<>();
        if (ticketsToCancel.isEmpty()) {
            conflictErrors.add("no tickets to cancel found!");
        }
        for (DetailedTicketDto t : ticketsToCancel) {
            if (t.getForPerformance().getStartTime().isBefore(LocalDateTime.now())) {
                validationErrors.add("cannot cancel tickets for performances that already started");
                break;
            }
        }
        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation cancellation of order failed ", validationErrors);
        }
        if (!conflictErrors.isEmpty()) {
            throw new ConflictException("cancellation of tickets of order failed because of conflicts ", validationErrors);
        }
    }
}
