package at.ac.tuwien.sepm.groupphase.backend.service;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CreateOrderDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedOrderDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.SimpleAddressDto;
import at.ac.tuwien.sepm.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.exception.ValidationException;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    /**
     * Create a new order entry.
     *
     * @param createOrderDto the order entry to create
     * @param uid            the uid of the user which creates the order
     * @return the created order entry
     * @throws NotFoundException       when User not found
     * @throws ValidationException     when the order to create is not valid
     * @throws BadCredentialsException when user is not logged in / another user tries to make order with other user id
     */
    DetailedOrderDto createOrder(CreateOrderDto createOrderDto, UUID uid) throws NotFoundException, ValidationException, BadCredentialsException;

    /**
     * Find all orders of a User.
     *
     * @param uid the uid of the user which creates the order
     * @return the found order entries
     * @throws NotFoundException       when User not found
     * @throws BadCredentialsException when user is not logged in / another user tries to view orders of other user
     */
    List<DetailedOrderDto> getOrderByUuid(UUID uid) throws NotFoundException, BadCredentialsException;

    /**
     * Find the address of the last order of a User.
     *
     * @param uid the uid of the user
     * @return the address used in last order of the user
     * @throws NotFoundException       when User not found
     * @throws BadCredentialsException when user is not logged in / another user tries to get address of other user
     */
    SimpleAddressDto getAddressOfLastOrder(UUID uid) throws NotFoundException, BadCredentialsException;

    /**
     * Buy Tickets of a Reservation Order.
     *
     * @param order order where tickets should be bought
     * @return the newly Created Order
     * @throws NotFoundException       when the order that should be updated is not found
     * @throws BadCredentialsException when user is not logged in / another user tries to view orders of other user
     *@throws ConflictException when changed data is in conflict with persistent data
     */
    DetailedOrderDto buyTicketsOfReservationOrder(DetailedOrderDto order) throws NotFoundException, BadCredentialsException, ConflictException;

    /**
     * Cancel Tickets of an Order.
     *
     * @param order order where tickets should be canceled
     * @return the newly Created Cancellation Order
     * @throws NotFoundException       when the order that should be updated is not found
     * @throws BadCredentialsException when user is not logged in / another user tries to cancel orders of other user
     * @throws ConflictException when changed data is in conflict with persistent data
     * @throws ValidationException when cancellation data is not valid
     */
    DetailedOrderDto cancelTicketsOfOrder(DetailedOrderDto order) throws NotFoundException, BadCredentialsException, ConflictException, ValidationException;

    /**
     * Checks if any tickets have to be removed from reservation orders.
     */
    void deleteOrdersBeforePerformanceStart();
}
