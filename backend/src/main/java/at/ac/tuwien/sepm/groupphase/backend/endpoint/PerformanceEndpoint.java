package at.ac.tuwien.sepm.groupphase.backend.endpoint;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CreatePerformanceDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedPerformanceDto;
import at.ac.tuwien.sepm.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepm.groupphase.backend.service.PerformanceService;
import jakarta.annotation.security.PermitAll;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.lang.invoke.MethodHandles;
import java.util.List;


@RestController
@RequestMapping(value = "/api/v1/performance")
public class PerformanceEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final PerformanceService performanceService;

    public PerformanceEndpoint(PerformanceService performanceService) {
        this.performanceService = performanceService;
    }

    @PostMapping
    @Secured("ROLE_ADMIN")
    @ResponseStatus(HttpStatus.CREATED)
    public DetailedPerformanceDto createPerformance(@Valid @RequestBody CreatePerformanceDto performance) throws ValidationException, ConflictException {
        LOGGER.info("POST /api/v1/performance body: {}", performance);
        return this.performanceService.createPerformance(performance);
    }

    @GetMapping
    @PermitAll
    @ResponseStatus(HttpStatus.OK)
    public List<DetailedPerformanceDto> findAll() {
        LOGGER.info("GET /api/v1/performance");
        return performanceService.findAll();
    }

    @GetMapping(value = "/{id}")
    @PermitAll
    @ResponseStatus(HttpStatus.OK)
    public DetailedPerformanceDto findById(@PathVariable Long id) throws NotFoundException {
        LOGGER.info("GET /api/v1/performance/{}", id);
        return performanceService.findById(id);
    }

    @DeleteMapping(value = "/{id}")
    @Secured("ROLE_ADMIN")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable Long id) throws NotFoundException {
        LOGGER.info("DELETE /api/v1/performance/{}", id);
        performanceService.deleteById(id);
    }

    @PutMapping(value = "/{id}")
    @Secured("ROLE_ADMIN")
    @ResponseStatus(HttpStatus.OK)
    public DetailedPerformanceDto updatePerformance(@PathVariable("id") Long id, @Valid @RequestBody DetailedPerformanceDto detailedPerformanceDto)
        throws NotFoundException, ValidationException, ConflictException {
        LOGGER.info("PUT /api/v1/performance/{}", id);
        return performanceService.updatePerformance(id, detailedPerformanceDto);
    }
}
