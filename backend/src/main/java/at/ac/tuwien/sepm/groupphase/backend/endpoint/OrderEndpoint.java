package at.ac.tuwien.sepm.groupphase.backend.endpoint;


import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CreateOrderDto;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedOrderDto;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.SimpleAddressDto;
import at.ac.tuwien.sepm.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepm.groupphase.backend.service.OrderService;
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

@RestController
@RequestMapping(value = "/api/v1/orders")
@RequiredArgsConstructor
public class OrderEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final OrderService orderService;

    @GetMapping(value = "/users/{uid}")
    @Secured({"ROLE_USER"})
    @ResponseStatus(HttpStatus.OK)
    public List<DetailedOrderDto> getOrdersByUuid(@PathVariable("uid") UUID id) throws NotFoundException {
        LOGGER.info("GET /api/v1/orders/users/{}", id);
        return orderService.getOrderByUuid(id);
    }

    @GetMapping(value = "/users/address/{uid}")
    @Secured({"ROLE_USER"})
    @ResponseStatus(HttpStatus.OK)
    public SimpleAddressDto getAddressOfLastOrder(@PathVariable("uid") UUID id) throws NotFoundException {
        LOGGER.info("GET /api/v1/orders/users/{}/address", id);
        return orderService.getAddressOfLastOrder(id);
    }

    @PostMapping(value = "/users/{uid}")
    @Secured({"ROLE_USER"})
    @ResponseStatus(HttpStatus.CREATED)
    public DetailedOrderDto createOrder(@PathVariable("uid") UUID id, @Valid @RequestBody CreateOrderDto createOrderDto) throws IllegalArgumentException, NotFoundException, ValidationException {
        LOGGER.info("POST /api/v1/orders/users/{}", id);
        return orderService.createOrder(createOrderDto, id);
    }

    @PutMapping(value = "/buy/{id}")
    @Secured({"ROLE_USER"})
    @ResponseStatus(HttpStatus.OK)
    public DetailedOrderDto buyTicketsOfOrder(@PathVariable("id") Long id, @Valid @RequestBody DetailedOrderDto detailedOrderDto) throws IllegalArgumentException, NotFoundException, ConflictException {
        LOGGER.info("PUT /api/v1/orders/buy/{}", id);
        return orderService.buyTicketsOfReservationOrder(detailedOrderDto);
    }

    @PutMapping(value = "/cancel/{id}")
    @Secured({"ROLE_USER"})
    @ResponseStatus(HttpStatus.OK)
    public DetailedOrderDto cancelTicketsOfOrder(@PathVariable("id") Long id, @Valid @RequestBody DetailedOrderDto detailedOrderDto) throws IllegalArgumentException, NotFoundException, ValidationException, ConflictException {
        LOGGER.info("DELETE /api/v1/orders/cancel/{}", id);
        return orderService.cancelTicketsOfOrder(detailedOrderDto);
    }
}
