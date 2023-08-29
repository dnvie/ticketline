package at.ac.tuwien.sepm.groupphase.backend.endpoint;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedCreateSeatDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedSeatDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.SimpleSeatDto;
import at.ac.tuwien.sepm.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepm.groupphase.backend.service.SeatService;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/seats")
public class SeatEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final SeatService seatService;

    @GetMapping
    @PermitAll
    public List<SimpleSeatDto> getAllSeats() {
        LOGGER.info("GET /api/v1/seats");
        return seatService.getAllSeats();
    }

    @GetMapping(value = "/{id}")
    @PermitAll
    public SimpleSeatDto getSeatById(@PathVariable UUID id) throws NotFoundException {
        LOGGER.info("GET /api/v1/seats/{}", id);
        return seatService.getSeatById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PermitAll
    public DetailedSeatDto createSeat(@RequestBody @Valid DetailedCreateSeatDto seatDto) throws NotFoundException, ValidationException, ConflictException {
        LOGGER.info("POST /api/v1/seats body: {}", seatDto);
        return seatService.createSeat(seatDto);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Secured("ROLE_ADMIN")
    public SimpleSeatDto updateSeat(@RequestBody @Valid SimpleSeatDto seatDto, @PathVariable UUID id) throws NotFoundException, ConflictException {
        LOGGER.info("PUT /api/v1/seats body: {}", seatDto);
        return seatService.updateSeat(seatDto, id);
    }

}