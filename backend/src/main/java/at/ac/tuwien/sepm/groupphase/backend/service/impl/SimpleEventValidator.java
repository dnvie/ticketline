package at.ac.tuwien.sepm.groupphase.backend.service.impl;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CreateEventDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.UpdateEventDto;
import at.ac.tuwien.sepm.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepm.groupphase.backend.exception.ValidationException;
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
public class SimpleEventValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final TicketRepository ticketRepository;

    public void validateForCreate(CreateEventDto createEventDto) throws ValidationException {
        LOGGER.debug("validateForCreate({})", createEventDto);
        List<String> validationErrors = new ArrayList<>();

        //validate createEventDto
        if (createEventDto.getType() == null) {
            validationErrors.add("Type must not be null");
        }
        if (createEventDto.getTitle() == null) {
            validationErrors.add("Title must not be null");
        } else if (createEventDto.getTitle().length() > 500) {
            validationErrors.add("Title must not be longer than 500 characters");
        }

        if (createEventDto.getBeginDate() == null) {
            validationErrors.add("Begin date must not be null");
        }

        if (createEventDto.getEndDate() == null) {
            validationErrors.add("End date must not be null");
        }
        if (createEventDto.getBeginDate() != null && createEventDto.getEndDate() != null
            && createEventDto.getBeginDate().isAfter(createEventDto.getEndDate())) {
            validationErrors.add("Begin date must not be after end date");
        }

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation of Event to create failed", validationErrors);
        }

    }

    public void validateForDelete(Long id) throws ConflictException {
        LOGGER.debug("validateForDelete({})", id);
        List<String> conflictErrors = new ArrayList<>();
        Long ticketCount = ticketRepository.findTicketsOfEvent(id);
        if (!(ticketCount == 0)) {
            conflictErrors.add("Can not delete event, because tickets of the event are already sold");
        }
        if (!conflictErrors.isEmpty()) {
            throw new ConflictException("Conflict errors occurred while deleting event", conflictErrors);
        }


    }

    public void validateBeforeUpdate(UpdateEventDto updateEventDto) throws ValidationException {
        LOGGER.debug("validateBeforeUpdate({})", updateEventDto);
        List<String> validationErrors = new ArrayList<>();

        //validate createEventDto
        if (updateEventDto.getType() == null) {
            validationErrors.add("Type must not be null");
        }
        if (updateEventDto.getTitle() == null) {
            validationErrors.add("Title must not be null");
        } else if (updateEventDto.getTitle().length() > 500) {
            validationErrors.add("Title must not be longer than 500 characters");
        }

        if (updateEventDto.getBeginDate() == null) {
            validationErrors.add("Begin date must not be null");
        }

        if (updateEventDto.getEndDate() == null) {
            validationErrors.add("End date must not be null");
        }
        if (updateEventDto.getBeginDate() != null && updateEventDto.getEndDate() != null
            && updateEventDto.getBeginDate().isAfter(updateEventDto.getEndDate())) {
            validationErrors.add("Begin date must not be after end date");
        }

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation of Event to update failed", validationErrors);
        }
    }

}
