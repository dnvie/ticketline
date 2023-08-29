package at.ac.tuwien.sepm.groupphase.backend.endpoint;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedEventDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.EventsWithCountDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.SearchDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.SearchResultsDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CreateEventDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.TopTenEventDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.UpdateEventDto;
import at.ac.tuwien.sepm.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepm.groupphase.backend.service.EventService;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/events")
@RequiredArgsConstructor
public class EventEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final EventService eventService;


    @GetMapping
    @PermitAll
    public EventsWithCountDto findAll(@RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "12") int size) {
        LOGGER.info("GET /api/v1/events");
        Pageable pageable = PageRequest.of(page, size);
        return new EventsWithCountDto(eventService.findAll(pageable), eventService.getTotalEventCount());
    }


    @GetMapping("/search")
    @PermitAll
    public SearchResultsDto search(SearchDto searchDto) {
        LOGGER.info("GET /api/v1/events/search Request parameters: {}", searchDto);
        return eventService.search(searchDto);
    }

    @GetMapping(value = "/{id}")
    @PermitAll
    public DetailedEventDto findOne(@PathVariable("id") Long id) throws NotFoundException {
        LOGGER.info("GET /api/v1/events/{}", id);
        return eventService.findOne(id);
    }

    @GetMapping(value = "/find/artist/{name}")
    @PermitAll
    public List<DetailedEventDto> findEventsOfArtist(@PathVariable("name") String name) throws NotFoundException {
        LOGGER.info("GET /api/v1/events/find/artist/{}", name);
        return eventService.findEventsByPerformerName(name);
    }

    @GetMapping(value = "/find/location/{name}")
    @PermitAll
    public List<DetailedEventDto> findEventsByLocation(@PathVariable("name") String name) throws NotFoundException {
        LOGGER.info("GET /api/v1/events/find/location/{}", name);
        return eventService.findEventsByLocation(name);
    }

    @GetMapping(value = "/top10")
    @PermitAll
    public List<TopTenEventDto> getTopTenEvents(@RequestParam(name = "type", required = false) String type) {
        LOGGER.info("GET /api/v1/events/top10");
        LocalDate currentDate = LocalDate.now();
        int requestedYear = currentDate.getYear();
        int requestedMonth = currentDate.getMonthValue();
        return eventService.getTopTenEvents(requestedYear, requestedMonth, type);
    }

    @PostMapping
    @Secured("ROLE_ADMIN")
    @ResponseStatus(HttpStatus.CREATED)
    public DetailedEventDto createEvent(@Valid @RequestBody CreateEventDto createEventDto)
        throws IllegalArgumentException, ValidationException, ConflictException {
        LOGGER.info("POST /api/v1/events");
        return eventService.createEvent(createEventDto);
    }

    @PutMapping(value = "/{id}")
    @Secured("ROLE_ADMIN")
    @ResponseStatus(HttpStatus.OK)
    public DetailedEventDto updateEvent(@PathVariable("id") Long id, @Valid @RequestBody UpdateEventDto updateEventDto)
        throws NotFoundException, IllegalArgumentException, ValidationException, ConflictException {
        LOGGER.info("PUT /api/v1/events/{}", id);
        return eventService.updateEvent(id, updateEventDto);
    }

    @DeleteMapping(value = "/{id}")
    @Secured("ROLE_ADMIN")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEvent(@PathVariable("id") Long id) throws NotFoundException, ConflictException {
        LOGGER.info("DELETE /api/v1/events/{}", id);
        eventService.deleteEvent(id);
    }

}
