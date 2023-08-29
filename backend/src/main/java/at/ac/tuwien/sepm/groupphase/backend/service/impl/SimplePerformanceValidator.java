package at.ac.tuwien.sepm.groupphase.backend.service.impl;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CreatePerformanceDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedPerformanceDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.SimplePerformanceDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.Event;
import at.ac.tuwien.sepm.groupphase.backend.entity.Location;
import at.ac.tuwien.sepm.groupphase.backend.entity.Performance;
import at.ac.tuwien.sepm.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepm.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepm.groupphase.backend.repository.EventRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.LocationRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.PerformanceRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class SimplePerformanceValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final LocationRepository locationRepository;
    private final EventRepository eventRepository;

    private final TicketRepository ticketRepository;

    private final PerformanceRepository performanceRepository;

    public SimplePerformanceValidator(LocationRepository locationRepository, EventRepository eventRepository,
                                      PerformanceRepository performanceRepository, TicketRepository ticketRepository) {
        this.locationRepository = locationRepository;
        this.eventRepository = eventRepository;
        this.performanceRepository = performanceRepository;
        this.ticketRepository = ticketRepository;
    }

    //TODO add validation for update

    public void validateForCreate(CreatePerformanceDto performance) throws ValidationException, ConflictException {

        List<String> validationErrors = new ArrayList<>();
        List<String> conflictErrors = new ArrayList<>();
        LOGGER.trace("validateForCreate({})", performance);
        //validate title
        if (performance.getTitle() == null || performance.getTitle().isBlank()) {
            validationErrors.add("No title given ");
        }
        if (performance.getTitle() != null && (performance.getTitle().length() > 100)) {
            validationErrors.add("The given title is to long ");
        }
        //validate Location
        if (performance.getLocation() != null) {
            Location location = locationRepository.findLocationById(performance.getLocation().getId());
            if (location == null) {
                conflictErrors.add("The given Location does not exist");
            }
        }
        //validate Event
        if (performance.getEvent() != null) {
            Event event = eventRepository.findEventById(performance.getEvent().getId());
            if (event == null) {
                conflictErrors.add("The given Event does not exist");
            }
        }
        //validate Performers
        if (performance.getPerformers().isEmpty()) {
            validationErrors.add("There has to be at least one performer");
        }
        for (String performer : performance.getPerformers()) {
            if (performer.isBlank() || performer.length() > 100) {
                validationErrors.add("not all performers are valid (must not be blank and the maximum length for one performer is 100)");
                break;
            }
        }
        //validate start and end date
        if (performance.getEvent() != null) {
            Event toCompare = eventRepository.findEventById(performance.getEvent().getId());
            if (toCompare != null && toCompare.getBeginDate() != null && toCompare.getEndDate() != null) {
                if (performance.getStartTime().toLocalDate().isBefore(toCompare.getBeginDate())) {
                    conflictErrors.add("the start date of the performance is before the start date of the event");
                }
                if (performance.getEndTime().toLocalDate().isAfter(toCompare.getEndDate())) {
                    conflictErrors.add("the end date of the performance is after the end date of the event");
                }
            }
            if (performance.getStartTime().isAfter(performance.getEndTime()) || performance.getStartTime().isEqual(performance.getEndTime())) {
                validationErrors.add("the given start time is before or equal to the given end time");
            }
        }
        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation of performance for create failed ", validationErrors);
        }
        if (!conflictErrors.isEmpty()) {
            throw new ConflictException("Creation of performance for create failed: ", conflictErrors);
        }
    }

    public void validateForCreateViaEvent(CreatePerformanceDto performance) throws ValidationException, ConflictException {

        List<String> validationErrors = new ArrayList<>();
        List<String> conflictErrors = new ArrayList<>();
        LOGGER.trace("validateForCreateViaEvent({})", performance);
        //validate title
        if (performance.getTitle() == null || performance.getTitle().isBlank()) {
            validationErrors.add("No title given ");
        }
        if (performance.getTitle() != null && (performance.getTitle().length() > 100)) {
            validationErrors.add("The given title is to long ");
        }
        //validate Location
        if (performance.getLocation() != null) {
            Location location = locationRepository.findLocationById(performance.getLocation().getId());
            if (location == null) {
                conflictErrors.add("The given Location does not exist");
            }
        }
        //validate Performers
        if (performance.getPerformers().isEmpty()) {
            validationErrors.add("There has to be at least one performer");
        }
        for (String performer : performance.getPerformers()) {
            if (performer.isBlank() || performer.length() > 100) {
                validationErrors.add("not all performers are valid (must not be blank and the maximum length for one performer is 100)");
                break;
            }
        }
        if (performance.getStartTime().isAfter(performance.getEndTime()) || performance.getStartTime().isEqual(performance.getEndTime())) {
            validationErrors.add("the given start time is before or equal to the given end time");
        }


        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation of performance for create failed ", validationErrors);
        }
        if (!conflictErrors.isEmpty()) {
            throw new ConflictException("Creation of performance for create failed: ", conflictErrors);
        }
    }

    public void validateBeforeUpdate(DetailedPerformanceDto performance) throws ValidationException, ConflictException {
        List<String> validationErrors = new ArrayList<>();
        List<String> conflictErrors = new ArrayList<>();
        LOGGER.trace("validateForUpdate({})", performance);
        //validate title
        if (performance.getTitle() == null || performance.getTitle().isBlank()) {
            validationErrors.add("No title given ");
        }
        if (performance.getTitle() != null && (performance.getTitle().length() > 100)) {
            validationErrors.add("The given title is to long ");
        }
        //validate Location
        if (performance.getLocation() != null) {
            Location location = locationRepository.findLocationById(performance.getLocation().getId());
            if (location == null) {
                conflictErrors.add("The given Location does not exist");
            }
        }
        //validate Event
        if (performance.getEvent() != null) {
            Event event = eventRepository.findEventById(performance.getEvent().getId());
            if (event == null) {
                conflictErrors.add("The given Event does not exist");
            }
        }
        //validate Performers
        if (performance.getPerformers().isEmpty()) {
            validationErrors.add("There has to be at least one performer");
        }
        for (String performer : performance.getPerformers()) {
            if (performer.isBlank() || performer.length() > 100) {
                validationErrors.add("not all performers are valid (must not be blank and the maximum length for one performer is 100)");
                break;
            }
        }
        //validate start and end date
        if (performance.getEvent() != null) {
            Event toCompare = eventRepository.findEventById(performance.getEvent().getId());
            if (toCompare != null && toCompare.getBeginDate() != null && toCompare.getEndDate() != null) {
                if (performance.getStartTime().toLocalDate().isBefore(toCompare.getBeginDate())) {
                    conflictErrors.add("the start date of the performance is before the start date of the event");
                }
                if (performance.getEndTime().toLocalDate().isAfter(toCompare.getEndDate())) {
                    conflictErrors.add("the end date of the performance is after the end date of the event");
                }
            }
            if (performance.getStartTime().isAfter(performance.getEndTime()) || performance.getStartTime().isEqual(performance.getEndTime())) {
                validationErrors.add("the given start time is before or equal to the given end time");
            }
        }

        conflictErrors.addAll(validateSeatMapForPerformance(performance.getSeatMap(), performanceRepository.findById(performance.getId())));

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation of performance for create failed ", validationErrors);
        }
        if (!conflictErrors.isEmpty()) {
            throw new ConflictException("Creation of performance for create failed: ", conflictErrors);
        }
    }

    private List<String> validateSeatMapForPerformance(UUID seatMap, Optional<Performance> oldPerformance) {
        LOGGER.trace("validateSeatMapForPerformance({}, {})", seatMap, oldPerformance);
        List<String> conflictErrors = new ArrayList<>();
        if (oldPerformance.isEmpty()) {
            return conflictErrors;
        }
        Performance old = oldPerformance.get();
        if (old.getSeatMap() == null) {
            return conflictErrors;
        }
        if (seatMap == null) {
            conflictErrors.add("The seat map of the performance can not be changed because there are already tickets sold for this performance");
            return conflictErrors;
        }
        if (seatMap.equals(old.getSeatMap().getId())) {
            return conflictErrors;
        }
        if (ticketRepository.findAllByPerformance(old.getId()).isEmpty()) {
            return conflictErrors;
        } else {
            conflictErrors.add("The seat map of the performance can not be changed because there are already tickets sold for this performance");
        }
        return conflictErrors;
    }

    public void validateBeforeUpdateViaEvent(SimplePerformanceDto performance) throws ValidationException, ConflictException {
        List<String> validationErrors = new ArrayList<>();
        List<String> conflictErrors = new ArrayList<>();
        LOGGER.trace("validateForUpdate({})", performance);
        //validate title
        if (performance.getTitle() == null || performance.getTitle().isBlank()) {
            validationErrors.add("No title given ");
        }
        if (performance.getTitle() != null && (performance.getTitle().length() > 100)) {
            validationErrors.add("The given title is to long ");
        }
        //validate Location
        if (performance.getLocation() != null) {
            Location location = locationRepository.findLocationById(performance.getLocation().getId());
            if (location == null) {
                conflictErrors.add("The given Location does not exist");
            }
        }

        //validate Performers
        if (performance.getPerformers().isEmpty()) {
            validationErrors.add("There has to be at least one performer");
        }
        for (String performer : performance.getPerformers()) {
            if (performer.isBlank() || performer.length() > 100) {
                validationErrors.add("not all performers are valid (must not be blank and the maximum length for one performer is 100)");
                break;
            }
        }
        //validate start and end date
        if (performance.getStartTime().isAfter(performance.getEndTime()) || performance.getStartTime().isEqual(performance.getEndTime())) {
            validationErrors.add("the given start time is before or equal to the given end time");
        }
        if (performance.getId() != null) {
            conflictErrors.addAll(validateSeatMapForPerformance(performance.getSeatMap(), performanceRepository.findById(performance.getId())));
        }


        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation of performance for create failed ", validationErrors);
        }
        if (!conflictErrors.isEmpty()) {
            throw new ConflictException("Creation of performance for create failed: ", conflictErrors);
        }

    }
}
