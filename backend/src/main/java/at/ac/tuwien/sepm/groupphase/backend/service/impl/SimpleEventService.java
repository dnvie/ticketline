package at.ac.tuwien.sepm.groupphase.backend.service.impl;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedEventDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.SearchDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.SearchResultsDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CreateEventDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.TopTenEventDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.UpdateEventDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CreatePerformanceDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.SimplePerformanceDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper.EventMapper;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper.LocationMapper;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper.PerformanceMapper;
import at.ac.tuwien.sepm.groupphase.backend.entity.Event;
import at.ac.tuwien.sepm.groupphase.backend.entity.Location;
import at.ac.tuwien.sepm.groupphase.backend.entity.Performance;
import at.ac.tuwien.sepm.groupphase.backend.entity.SeatMap;
import at.ac.tuwien.sepm.groupphase.backend.entity.enums.EventType;
import at.ac.tuwien.sepm.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepm.groupphase.backend.exception.FatalException;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepm.groupphase.backend.repository.EventRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.LocationRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.PerformanceRepository;
import at.ac.tuwien.sepm.groupphase.backend.service.EventService;
import jakarta.persistence.EntityManager;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class SimpleEventService implements EventService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final EventRepository eventRepository;

    private final EventMapper eventMapper;

    private final SimpleEventValidator simpleEventValidator;

    private final SimplePerformanceValidator simplePerformanceValidator;
    private final PerformanceMapper performanceMapper;
    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;
    private final PerformanceRepository performanceRepository;
    private final EntityManager em;

    @Override
    public List<DetailedEventDto> findAll(Pageable pageable) {
        LOGGER.debug("Find all events with pagination");
        List<Event> events = eventRepository.findAllWithPerformancesAndLocation(pageable);
        List<DetailedEventDto> eventDtos = new ArrayList<>();
        for (Event event : events) {
            List<String> performers = eventRepository.findAllPerformers(event.getId());
            DetailedEventDto eventDto = eventMapper.eventToDetailedEventDto(event);
            eventDto.setPerformers(performers);
            eventDtos.add(eventDto);
        }
        return eventDtos;
    }

    @Override
    public DetailedEventDto createEvent(CreateEventDto createEventDto) throws ValidationException, ConflictException {
        LOGGER.debug("createEvent({})", createEventDto);
        simpleEventValidator.validateForCreate(createEventDto);
        Event event = eventMapper.createEventDtoToEvent(createEventDto);
        if (createEventDto.getPerformances() != null) {
            for (CreatePerformanceDto performance : createEventDto.getPerformances()) {
                simplePerformanceValidator.validateForCreateViaEvent(performance);
            }
            for (Performance performance : event.getPerformances()) {
                performance.setEvent(event);
            }
        }
        event = eventRepository.save(event);
        try {
            return getDetailedEvent(event.getId());
        } catch (NotFoundException e) {
            //TODO: If handled in global remove this logger
            LOGGER.error("Could not find event with id {}", event.getId());
            throw new FatalException(String.format("Could not find event with id %d", event.getId()), e);
        }
    }

    @Override
    @Transactional
    public void deleteEvent(Long id) throws NotFoundException, ConflictException {
        LOGGER.debug("deleteEvent({})", id);
        simpleEventValidator.validateForDelete(id);
        Long deletedCount = eventRepository.deleteEventById(id);
        if (deletedCount < 1) {
            throw new NotFoundException(String.format("Could not find event with id %d", id));
        } else if (deletedCount > 1) {
            //TODO: If handled in global remove this logger
            LOGGER.error("Expected 1 event to be deleted, but {} were deleted", deletedCount);
            throw new FatalException(String.format("Expected 1 event to be deleted, but %d were deleted", deletedCount));
        }
    }

    @Override
    @Transactional
    public DetailedEventDto updateEvent(Long id, UpdateEventDto updateEventDto) throws NotFoundException, ValidationException, ConflictException {
        LOGGER.debug("updateEvent({})", id);
        simpleEventValidator.validateBeforeUpdate(updateEventDto);
        for (SimplePerformanceDto p : updateEventDto.getPerformances()) {
            simplePerformanceValidator.validateBeforeUpdateViaEvent(p);
        }
        Optional<Event> toUpdate = eventRepository.findById(id);
        if (toUpdate.isEmpty()) {
            throw new NotFoundException(String.format("Could not find event with id %d", id));
        }
        Event event = toUpdate.get();
        event.setEndDate(updateEventDto.getEndDate());
        event.setBeginDate(updateEventDto.getBeginDate());
        event.setTitle(updateEventDto.getTitle());
        event.setType(updateEventDto.getType());
        event.setImage(updateEventDto.getImage());
        Set<Performance> performances = event.getPerformances();
        performances.clear();
        performances.addAll(performanceMapper.simplePerformanceDtoToPerformance(updateEventDto.getPerformances()));
        Event updatedEvent = eventRepository.save(event);
        for (Performance performance : event.getPerformances()) {
            performance.setEvent(updatedEvent);
        }
        try {
            return getDetailedEvent(event.getId());
        } catch (NotFoundException e) {
            //TODO: If handled in global remove this logger
            LOGGER.error("Could not find event with id {}", event.getId());
            throw new FatalException(String.format("Could not find event with id %d", event.getId()), e);
        }
    }

    private DetailedEventDto getDetailedEvent(Long id) throws NotFoundException {
        LOGGER.trace("getDetailedEvent({})", id);
        Optional<Event> createdEvent = eventRepository.findByIdWithPerformancesAndLocation(id);
        if (createdEvent.isEmpty()) {
            throw new NotFoundException(String.format("Could not find event with id %d", id));
        }
        return eventMapper.eventToDetailedEventDto(createdEvent.get());
    }

    @Override
    public DetailedEventDto findOne(Long id) throws NotFoundException {
        LOGGER.debug("findOne({})", id);
        Optional<Event> event = eventRepository.findByIdWithPerformancesAndLocation(id);
        if (event.isPresent()) {
            List<String> performers = eventRepository.findAllPerformers(id);
            DetailedEventDto eventDto = eventMapper.eventToDetailedEventDto(event.get());
            eventDto.setPerformers(performers);
            return eventDto;
        } else {
            throw new NotFoundException(String.format("Could not find event with id %d", id));
        }
    }

    @Override
    //TODO: stop n+1 problem here
    @Transactional
    public SearchResultsDto search(SearchDto searchDto) {
        LOGGER.debug("searchByParameters({})", searchDto);
        SearchResultsDto toReturn = new SearchResultsDto();
        final boolean searchBarOnly = Stream.of(searchDto.getCity(), searchDto.getZip(), searchDto.getCountry(), searchDto.getStreet(), searchDto.getType(), searchDto.getStart(), searchDto.getEnd(),
            searchDto.getPerformanceTime(), searchDto.getPerformanceDate(), searchDto.getMaxPrice(), searchDto.getMinPrice(), searchDto.getEventName(), searchDto.getRoomName()).allMatch(property -> property == null);
        boolean noLocationParameters = Stream.of(searchDto.getCity(), searchDto.getZip(), searchDto.getCountry(), searchDto.getStreet())
            .allMatch(property -> property == null);
        boolean noEventParameters = Stream.of(searchDto.getStart(), searchDto.getEnd(), searchDto.getType())
            .allMatch(property -> property == null);
        boolean noPerformanceParameters = Stream.of(searchDto.getPerformanceTime(), searchDto.getPerformanceDate(), searchDto.getMaxPrice(),
            searchDto.getMinPrice(), searchDto.getEventName(), searchDto.getRoomName()).allMatch(property -> property == null);
        int falseCount = 0;
        if (!noLocationParameters) {
            falseCount++;
        }
        if (!noEventParameters) {
            falseCount++;
        }
        if (!noPerformanceParameters) {
            falseCount++;
        }
        boolean mixed = falseCount > 1;
        if (searchBarOnly) {
            toReturn.setArtists(eventRepository.findAllPerformersByName(searchDto.getSearchBar()));
            toReturn.setLocations(locationMapper.locationToSimpleLocationDto(locationRepository.findLocationsByNamePart(searchDto.getSearchBar())));
            toReturn.setEvents(eventMapper.eventsToDetailedEventDtos(eventRepository.findEventsByPartOfTitle(searchDto.getSearchBar())));
            toReturn.setPerformances(performanceMapper.performanceToDetailedPerformanceDto(performanceRepository.findPerformancesByNamePart(searchDto.getSearchBar())));
        } else if (!noEventParameters || mixed) {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Event> cq = cb.createQuery(Event.class);
            Root<Event> eventRoot = cq.from(Event.class);
            List<Predicate> predicates = new ArrayList<>();
            predicates.addAll(queryEventParameters(searchDto, cb, cq, eventRoot));
            if (!noLocationParameters) {
                predicates.addAll(queryLocationParametersForMixedSearch(searchDto, cb, cq, eventRoot));
            }
            if (!noPerformanceParameters) {
                predicates.addAll(queryPerformanceParametersForMixedSearch(searchDto, cb, cq, eventRoot));
            }
            cq.select(eventRoot).where(predicates.toArray(new Predicate[0]));
            List<Event> events = em.createQuery(cq).getResultList();
            toReturn.setEvents(eventMapper.eventsToDetailedEventDtos(events));
        } else if (!noLocationParameters) {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Location> cq = cb.createQuery(Location.class);
            Root<Location> locationRoot = cq.from(Location.class);
            List<Predicate> predicates = queryLocationParameters(searchDto, cb, cq, locationRoot);
            cq.select(locationRoot).where(predicates.toArray(new Predicate[0]));
            List<Location> locations = em.createQuery(cq).getResultList();
            toReturn.setLocations(locationMapper.locationToSimpleLocationDto(locations));
        } else {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Performance> cq = cb.createQuery(Performance.class);
            Root<Performance> performanceRoot = cq.from(Performance.class);
            List<Predicate> predicates = queryPerformanceParameters(searchDto, cb, cq, performanceRoot);
            cq.select(performanceRoot).where(predicates.toArray(new Predicate[0]));
            List<Performance> performances = em.createQuery(cq).getResultList();
            toReturn.setPerformances(performanceMapper.performanceToDetailedPerformanceDto(performances));
        }
        return toReturn;
    }

    private List<Predicate> queryEventParameters(SearchDto searchDto, CriteriaBuilder cb, CriteriaQuery cq, Root<Event> eventRoot) {
        LOGGER.trace("create query for event parameters of {}", searchDto);
        List<Predicate> predicates = new ArrayList<>();
        if (searchDto.getSearchBar() != null) {
            predicates.add(cb.like(cb.lower(eventRoot.get("title")), "%" + searchDto.getSearchBar() + "%"));
        }
        if (searchDto.getType() != null) {
            predicates.add(cb.equal(eventRoot.get("type"), searchDto.getType().ordinal()));
        }
        if (searchDto.getStart() != null && searchDto.getEnd() != null) {
            predicates.add(cb.greaterThanOrEqualTo(eventRoot.get("beginDate"), searchDto.getStart()));
            predicates.add(cb.lessThanOrEqualTo(eventRoot.get("endDate"), searchDto.getEnd()));
        } else {
            if (searchDto.getStart() != null) {
                predicates.add(cb.greaterThanOrEqualTo(eventRoot.get("beginDate"), searchDto.getStart()));
            }
            if (searchDto.getEnd() != null) {
                predicates.add(cb.lessThanOrEqualTo(eventRoot.get("endDate"), searchDto.getEnd()));
            }
        }

        return predicates;
    }

    private List<Predicate> queryLocationParametersForMixedSearch(SearchDto searchDto, CriteriaBuilder cb, CriteriaQuery cq, Root<Event> eventRoot) {
        LOGGER.trace("create query for location parameters of {}", searchDto);
        List<Predicate> predicates = new ArrayList<>();
        if (searchDto.getStreet() != null) {
            Join<Event, Performance> performanceJoin = eventRoot.join("performances");
            Join<Performance, Location> locationJoin = performanceJoin.join("location");
            predicates.add(cb.like(cb.lower(locationJoin.get("street")), "%" + searchDto.getStreet().toLowerCase() + "%"));
        }
        if (searchDto.getZip() != null) {
            Join<Event, Performance> performanceJoin = eventRoot.join("performances");
            Join<Performance, Location> locationJoin = performanceJoin.join("location");
            predicates.add(cb.equal(locationJoin.get("postalCode"), searchDto.getZip()));
        }
        if (searchDto.getCity() != null) {
            Join<Event, Performance> performanceJoin = eventRoot.join("performances");
            Join<Performance, Location> locationJoin = performanceJoin.join("location");
            predicates.add(cb.equal(cb.lower(locationJoin.get("city")), searchDto.getCity().toLowerCase()));
        }
        if (searchDto.getCountry() != null) {
            Join<Event, Performance> performanceJoin = eventRoot.join("performances");
            Join<Performance, Location> locationJoin = performanceJoin.join("location");
            predicates.add(cb.equal(cb.lower(locationJoin.get("country")), searchDto.getCountry().toLowerCase()));
        }
        return predicates;
    }

    private List<Predicate> queryLocationParameters(SearchDto searchDto, CriteriaBuilder cb, CriteriaQuery cq, Root<Location> locationRoot) {
        LOGGER.trace("create query for location parameters of {}", searchDto);
        List<Predicate> predicates = new ArrayList<>();
        if (searchDto.getSearchBar() != null) {
            predicates.add(cb.like(cb.lower(locationRoot.get("name")), "%" + searchDto.getSearchBar().toLowerCase() + "%"));
        }
        if (searchDto.getStreet() != null) {
            predicates.add(cb.like(cb.lower(locationRoot.get("street")), "%" + searchDto.getStreet().toLowerCase() + "%"));
        }
        if (searchDto.getCountry() != null) {
            predicates.add(cb.equal(cb.lower(locationRoot.get("country")), searchDto.getCountry().toLowerCase()));
        }
        if (searchDto.getZip() != null) {
            predicates.add(cb.equal(locationRoot.get("postalCode"), searchDto.getZip()));
        }
        if (searchDto.getCity() != null) {
            predicates.add(cb.equal(cb.lower(locationRoot.get("city")), searchDto.getCity().toLowerCase()));
        }
        return predicates;
    }

    private List<Predicate> queryPerformanceParameters(SearchDto searchDto, CriteriaBuilder cb, CriteriaQuery cq, Root<Performance> performanceRoot) {
        LOGGER.trace("create query for performance parameters of {}", searchDto);

        //TODO add query for RoomName and price (when tickets and location/price implementation is finished)

        List<Predicate> predicates = new ArrayList<>();
        if (searchDto.getSearchBar() != null) {
            predicates.add(cb.like(cb.lower(performanceRoot.get("title")), "%" + searchDto.getSearchBar() + "%"));
        }
        if (searchDto.getEventName() != null) {
            Join<Performance, Event> eventJoin = performanceRoot.join("event");
            predicates.add(cb.like(cb.lower(eventJoin.get("title")), "%" + searchDto.getEventName().toLowerCase() + "%"));
        }
        if (searchDto.getPerformanceDate() != null) {
            predicates.add(cb.equal(performanceRoot.get("startTime").as(LocalDate.class), searchDto.getPerformanceDate()));
        }
        if (searchDto.getPerformanceTime() != null) {
            predicates.add(cb.equal(performanceRoot.get("startTime").as(LocalTime.class), searchDto.getPerformanceTime()));
        }
        if (searchDto.getMinPrice() != null && searchDto.getMaxPrice() != null) {
            predicates.add(cb.between(performanceRoot.get("price"), searchDto.getMinPrice(), searchDto.getMaxPrice()));
        } else {
            if (searchDto.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(performanceRoot.get("price"), searchDto.getMinPrice()));
            }
            if (searchDto.getEnd() != null) {
                predicates.add(cb.lessThanOrEqualTo(performanceRoot.get("price"), searchDto.getMaxPrice()));
            }
        }
        if (searchDto.getMinPrice() != null && searchDto.getMaxPrice() != null) {
            predicates.add(cb.greaterThanOrEqualTo(performanceRoot.get("price"), BigDecimal.valueOf(searchDto.getMinPrice())));
            predicates.add(cb.lessThanOrEqualTo(performanceRoot.get("price"), BigDecimal.valueOf(searchDto.getMaxPrice())));
        } else {
            if (searchDto.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(performanceRoot.get("price"), BigDecimal.valueOf(searchDto.getMinPrice())));
            }
            if (searchDto.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(performanceRoot.get("price"), BigDecimal.valueOf(searchDto.getMaxPrice())));
            }
        }
        if (searchDto.getRoomName() != null) {
            Join<Performance, SeatMap> seatMapJoin = performanceRoot.join("seatMap");
            predicates.add(cb.like(cb.lower(seatMapJoin.get("name")), "%" + searchDto.getRoomName().toLowerCase() + "%"));
        }
        return predicates;
    }

    private List<Predicate> queryPerformanceParametersForMixedSearch(SearchDto searchDto, CriteriaBuilder cb, CriteriaQuery cq, Root<Event> eventRoot) {
        LOGGER.trace("create query for performance parameters of {}", searchDto);

        //TODO add query for RoomName and price (when tickets and location/price implementation is finished)

        List<Predicate> predicates = new ArrayList<>();
        Join<Event, Performance> performanceJoin = eventRoot.join("performances");
        if (searchDto.getEventName() != null) {
            predicates.add(cb.like(cb.lower(eventRoot.get("title")), "%" + searchDto.getEventName().toLowerCase() + "%"));
        }
        if (searchDto.getPerformanceDate() != null) {
            predicates.add(cb.equal(performanceJoin.get("startTime").as(LocalDate.class), searchDto.getPerformanceDate()));
        }
        if (searchDto.getPerformanceTime() != null) {
            predicates.add(cb.equal(performanceJoin.get("startTime").as(LocalTime.class), searchDto.getPerformanceTime()));
        }
        if (searchDto.getMinPrice() != null && searchDto.getMaxPrice() != null) {
            predicates.add(cb.greaterThanOrEqualTo(performanceJoin.get("price"), BigDecimal.valueOf(searchDto.getMinPrice())));
            predicates.add(cb.lessThanOrEqualTo(performanceJoin.get("price"), BigDecimal.valueOf(searchDto.getMaxPrice())));
        } else {
            if (searchDto.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(performanceJoin.get("price"), BigDecimal.valueOf(searchDto.getMinPrice())));
            }
            if (searchDto.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(performanceJoin.get("price"), BigDecimal.valueOf(searchDto.getMaxPrice())));
            }
        }
        if (searchDto.getRoomName() != null) {
            Join<Performance, SeatMap> seatMapJoin = performanceJoin.join("seatMap");
            predicates.add(cb.like(cb.lower(seatMapJoin.get("name")), "%" + searchDto.getRoomName().toLowerCase() + "%"));
        }
        return predicates;
    }

    @Override
    public List<DetailedEventDto> findEventsByPerformerName(String name) throws NotFoundException {
        LOGGER.debug("searchEventsByName({})", name);
        List<Event> events = eventRepository.findEventsMatchingToPerformerName(name);
        List<DetailedEventDto> eventDtos = new ArrayList<>();
        if (!events.isEmpty()) {
            for (Event e : events) {
                List<String> performers = eventRepository.findAllPerformers(e.getId());
                DetailedEventDto eventDto = eventMapper.eventToDetailedEventDto(e);
                eventDto.setPerformers(performers);
                eventDtos.add(eventDto);
            }
            return eventDtos;
        } else {
            throw new NotFoundException(String.format("Could not find an event where performer with name %s plays", name));
        }
    }

    @Override
    public List<DetailedEventDto> findEventsByLocation(String name) throws NotFoundException {
        LOGGER.debug("searchEventsByLocation({})", name);
        List<Event> events = eventRepository.findEventsMatchingToLocation(name);
        List<DetailedEventDto> eventDtos = new ArrayList<>();
        if (!events.isEmpty()) {
            for (Event e : events) {
                List<String> performers = eventRepository.findAllPerformers(e.getId());
                DetailedEventDto eventDto = eventMapper.eventToDetailedEventDto(e);
                eventDto.setPerformers(performers);
                eventDtos.add(eventDto);
            }
            return eventDtos;
        } else {
            throw new NotFoundException(String.format("Could not find an event where a performance is at %s", name));
        }
    }

    public List<TopTenEventDto> getTopTenEvents(int year, int month, String type) {
        LOGGER.debug("getTopEventsByTicketSalesPerMonth({},{},{})", year, month, type);
        Pageable pageable = PageRequest.of(0, 10);
        EventType eventType;
        if (type == null) {
            eventType = null;
        } else if (type.equalsIgnoreCase("CONCERT")) {
            eventType = EventType.CONCERT;
        } else if (type.equalsIgnoreCase("MUSICAL")) {
            eventType = EventType.MUSICAL;
        } else if (type.equalsIgnoreCase("THEATER")) {
            eventType = EventType.THEATER;
        } else if (type.equalsIgnoreCase("FESTIVAL")) {
            eventType = EventType.FESTIVAL;
        } else if (type.equalsIgnoreCase("MOVIE")) {
            eventType = EventType.MOVIE;
        } else if (type.equalsIgnoreCase("OPERA")) {
            eventType = EventType.OPERA;
        } else if (type.equalsIgnoreCase("OTHER")) {
            eventType = EventType.OTHER;
        } else {
            eventType = null;
        }
        return eventRepository.findTopEventsByTicketSalesPerMonth(year, month, eventType, pageable);
    }

    public Long getTotalEventCount() {
        return eventRepository.countAllEvents();
    }
}
