package at.ac.tuwien.sepm.groupphase.backend.endpoint;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CreateLocationDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.SimpleLocationDto;
import at.ac.tuwien.sepm.groupphase.backend.service.LocationService;
import jakarta.validation.Valid;
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

@RestController
@RequestMapping(value = "/api/v1/locations")
public class LocationEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final LocationService locationService;

    public LocationEndpoint(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping
    @Secured("ROLE_USER")
    @ResponseStatus(HttpStatus.OK)
    public List<SimpleLocationDto> findAll(@RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "0") int size) {
        LOGGER.info("GET /api/v1/locations");
        return locationService.findAll(page, size);
    }

    @GetMapping(value = "/{id}")
    @Secured("ROLE_USER")
    public SimpleLocationDto findOne(@PathVariable Long id) {
        LOGGER.info("GET /api/v1/locations/{}", id);
        return locationService.findOne(id);
    }

    @PostMapping
    @Secured("ROLE_ADMIN")
    @ResponseStatus(HttpStatus.CREATED)
    public SimpleLocationDto createLocation(@RequestBody @Valid CreateLocationDto createLocationDto) {
        LOGGER.info("POST /api/v1/locations body: {}", createLocationDto);
        return locationService.createLocation(createLocationDto);
    }

    @PutMapping(value = "/{id}")
    @Secured("ROLE_ADMIN")
    @ResponseStatus(HttpStatus.OK)
    public SimpleLocationDto updateLocation(@PathVariable Long id, @RequestBody @Valid SimpleLocationDto simpleLocationDto) {
        LOGGER.info("PUT /api/v1/locations body: {}", simpleLocationDto);
        return locationService.updateLocation(id, simpleLocationDto);
    }

    @DeleteMapping(value = "/{id}")
    @Secured("ROLE_ADMIN")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLocation(@PathVariable Long id) {
        LOGGER.info("DELETE /api/v1/locations/{}", id);
        locationService.deleteLocation(id);
    }

}
