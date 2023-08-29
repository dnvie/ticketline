package at.ac.tuwien.sepm.groupphase.backend.endpoint;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CreateSeatMapDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedSeatMapDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedSectorDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.SimpleSeatMapDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.SimpleSeatMapsWithCountDto;
import at.ac.tuwien.sepm.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepm.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepm.groupphase.backend.service.SeatMapSectorService;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/seatmaps")
@RequiredArgsConstructor
public class SeatMapSectorEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final SeatMapSectorService seatMapSectorService;


    @GetMapping
    @Secured("ROLE_USER")
    public SimpleSeatMapsWithCountDto getAllSeatMaps(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "0") int size) {
        LOGGER.info("GET /api/v1/seatmaps");
        return seatMapSectorService.getAllSeatMaps(page, size);
    }

    @GetMapping(value = "{id}")
    @PermitAll
    public DetailedSeatMapDto getSeatMapById(@PathVariable UUID id) {
        LOGGER.info("GET /api/v1/seatmaps/{}", id);
        return seatMapSectorService.getSeatMapById(id);
    }

    @GetMapping(value = "/sectors")
    @Secured("ROLE_USER")
    public List<DetailedSectorDto> getAllSectors() {
        LOGGER.info("GET /api/v1/sectors");
        return seatMapSectorService.getAllSectors();
    }

    @GetMapping(value = "/sectors/{id}")
    @Secured("ROLE_USER")
    public DetailedSectorDto getSectorById(@PathVariable UUID id) {
        LOGGER.info("GET /api/v1/sectors/{}", id);
        return seatMapSectorService.getSectorById(id);
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    @Secured("ROLE_ADMIN")
    public DetailedSeatMapDto createSeatMap(@RequestBody @Valid CreateSeatMapDto createSeatMapDto) throws ValidationException, ConflictException {
        LOGGER.info("POST /api/v1/sectors");
        return seatMapSectorService.createSeatMap(createSeatMapDto);
    }

    @PutMapping(value = "/sectors/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Secured("ROLE_ADMIN")
    public DetailedSectorDto updateSector(@PathVariable UUID id, @RequestBody @Valid DetailedSectorDto detailedSectorDto) throws ValidationException, ConflictException {
        LOGGER.info("PUT /api/v1/sectors/{}", id);
        return seatMapSectorService.updateSector(id, detailedSectorDto);
    }

    @PutMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Secured("ROLE_ADMIN")
    public DetailedSeatMapDto updateSeatMap(@PathVariable UUID id, @RequestBody @Valid DetailedSeatMapDto createSeatMapDto) throws ValidationException, ConflictException {
        LOGGER.info("PUT /api/v1/seatmaps/{}", id);
        return seatMapSectorService.updateSeatMap(id, createSeatMapDto);
    }

    @DeleteMapping(value = "{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Secured("ROLE_ADMIN")
    public void deleteSeatMap(@PathVariable UUID id) {
        LOGGER.info("DELETE /api/v1/seatmaps/{}", id);
        seatMapSectorService.deleteSeatMap(id);
    }
}
