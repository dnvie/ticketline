package at.ac.tuwien.sepm.groupphase.backend.service.impl;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CreateOrderDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedOrderDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedTicketDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.SimpleAddressDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper.OrderMapper;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper.SeatMapper;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper.TicketMapper;
import at.ac.tuwien.sepm.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepm.groupphase.backend.entity.Ticket;
import at.ac.tuwien.sepm.groupphase.backend.entity.Order;
import at.ac.tuwien.sepm.groupphase.backend.entity.enums.OrderType;
import at.ac.tuwien.sepm.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepm.groupphase.backend.repository.OrderRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.TicketRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepm.groupphase.backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class SimpleOrderService implements OrderService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final OrderMapper orderMapper;
    private final TicketMapper ticketMapper;
    private final TicketRepository ticketRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final SeatMapper seatMapper;
    private final SimpleOrderValidator simpleOrderValidator;

    @Scheduled(fixedDelay = 60000) // Runs every minute
    public void deleteOrdersBeforePerformanceStart() {
        LOGGER.debug("reservationDeletionCheck");
        LocalDateTime now = LocalDateTime.now().plusMinutes(30);
        List<Order> ordersToDelete = orderRepository.findOrdersByPerformanceTime(now);
        Set<Ticket> remaining = new HashSet<>();
        for (Order o : ordersToDelete) {
            for (Ticket t : o.getTickets()) {
                if (t.getForPerformance().getStartTime().isBefore(now)) {
                    ticketRepository.deleteById(t.getId());
                } else {
                    remaining.add(t);
                }
            }
            if (remaining.isEmpty()) {
                orderRepository.deleteById(o.getId());
            } else {
                BigDecimal totalPrice = BigDecimal.valueOf(0);
                for (Ticket t : remaining) {
                    totalPrice = totalPrice.add(t.getPrice());
                }
                o.setTotalPrice(totalPrice);
                o.setTickets(remaining);
                orderRepository.save(o);
                remaining.clear();
            }
        }
    }

    @Override
    public List<DetailedOrderDto> getOrderByUuid(UUID uid) throws NotFoundException, BadCredentialsException {
        LOGGER.debug("findOrderByUuid({})", uid);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        authenticate(auth, uid);
        List<Order> foundOrders = orderRepository.findOrdersByUuid(uid);
        for (Order o : foundOrders) {
            o.setTickets(ticketRepository.findTicketsByOrderId(o.getId()));
        }
        List<DetailedOrderDto> orders = orderMapper.orderToDetailedOrderDto(foundOrders);
        for (int i = 0; i < orders.size(); i++) {
            orders.get(i).setTickets(this.ticketMapper.ticketSetToDetailedTicketDtoSet(foundOrders.get(i).getTickets()));
        }
        return orders;
    }

    @Override
    public DetailedOrderDto createOrder(CreateOrderDto createOrderDto, UUID uid) throws NotFoundException, ValidationException, BadCredentialsException {
        LOGGER.debug("createOrder({})", createOrderDto);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        authenticate(auth, uid);
        this.simpleOrderValidator.validateOrderForCreate(createOrderDto, uid);
        Order toCreate = orderMapper.createOrderDtoToOrder(createOrderDto);
        toCreate.setTickets(null);
        toCreate.setOrderBy(userRepository.findUserById(uid).get(0));
        toCreate.setOrderNumber(generateOrderNumber(uid));
        Order created = orderRepository.save(toCreate);
        if (createOrderDto.getOrderType() == OrderType.BUY) {
            for (DetailedTicketDto t : createOrderDto.getTickets()) {
                Ticket ticketEntity = ticketMapper.detailedTicketDtoToTicket(t);
                ticketEntity.setOrder(created);
                ticketEntity.setForSeat(this.seatMapper.seatDtoToSeat(t.getSeat()));
                ticketEntity.setReserved(false);
                ticketRepository.save(ticketEntity);
            }
        } else if (createOrderDto.getOrderType() == OrderType.RESERVATION) {
            for (DetailedTicketDto t : createOrderDto.getTickets()) {
                Ticket ticketEntity = ticketMapper.detailedTicketDtoToTicket(t);
                ticketEntity.setOrder(created);
                ticketEntity.setForSeat(this.seatMapper.seatDtoToSeat(t.getSeat()));
                ticketEntity.setReserved(true);
                ticketRepository.save(ticketEntity);
            }
        }
        return this.orderMapper.orderToDetailedOrderDto(created);
    }

    @Override
    public DetailedOrderDto buyTicketsOfReservationOrder(DetailedOrderDto order) throws NotFoundException, ConflictException, BadCredentialsException {
        LOGGER.debug("buyTicketsOfReservationOrder ({})", order.getId());
        Order old = checkIfOrderExists(order.getId());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        authenticate(auth, old.getOrderBy().getId());
        List<DetailedTicketDto> ticketsToBuy = new ArrayList<>();
        List<DetailedTicketDto> ticketsToRemain = new ArrayList<>();
        for (DetailedTicketDto t : order.getTickets()) {
            if (!t.isReserved()) {
                ticketsToBuy.add(t);
            } else {
                ticketsToRemain.add(t);
            }
        }
        simpleOrderValidator.validateOrderForBuyingTicketsOfReservation(order, ticketsToBuy);
        LocalDateTime currentDateTime = LocalDateTime.now();
        if (ticketsToRemain.isEmpty()) {
            old.setOrderType(OrderType.BUY);
            old.setOrderDate(currentDateTime);
            old.setStreet(order.getStreet());
            old.setCity(order.getCity());
            old.setCountry(order.getCountry());
            old.setZip(order.getZip());
            old.setPaymentType(order.getPaymentType());
            old.setNumberOfCard(order.getNumberOfCard());
            Order updated = orderRepository.save(old);
            for (DetailedTicketDto t : order.getTickets()) {
                Ticket entity = ticketMapper.detailedTicketDtoToTicket(t);
                entity.setOrder(old);
                entity.setForSeat(this.seatMapper.seatDtoToSeat(t.getSeat()));
                entity.setReserved(false);
                ticketRepository.save(entity);
            }
            return this.orderMapper.orderToDetailedOrderDto(updated);
        } else {
            BigDecimal totalPrice = BigDecimal.valueOf(0);
            for (DetailedTicketDto t : ticketsToBuy) {
                totalPrice = totalPrice.add(t.getPrice());
            }
            List<ApplicationUser> forUsers = this.userRepository.findUserById(order.getOrderBy().getId());
            if (forUsers.isEmpty()) {
                throw new NotFoundException(String.format("Could not find user with uid %s", order.getOrderBy().getId()));
            }
            ApplicationUser forUser = forUsers.get(0);
            Set<DetailedTicketDto> set = new HashSet<>(ticketsToBuy);
            CreateOrderDto newOrder = new CreateOrderDto(currentDateTime, totalPrice, set, OrderType.BUY, order.getStreet(),
                order.getCity(), order.getCountry(), order.getZip(), order.getPaymentType(), order.getNumberOfCard());
            Order toCreate = orderMapper.createOrderDtoToOrder(newOrder);
            toCreate.setOrderBy(forUser);
            toCreate.setTickets(null);
            toCreate.setOrderNumber(generateOrderNumber(order.getOrderBy().getId()));
            Order created = orderRepository.save(toCreate);
            for (DetailedTicketDto t : ticketsToBuy) {
                Ticket ticketEntity = ticketMapper.detailedTicketDtoToTicket(t);
                ticketEntity.setOrder(created);
                ticketEntity.setForSeat(this.seatMapper.seatDtoToSeat(t.getSeat()));
                ticketEntity.setReserved(false);
                ticketRepository.save(ticketEntity);
            }
            BigDecimal totalPriceOld = BigDecimal.valueOf(0);
            for (DetailedTicketDto t : ticketsToRemain) {
                totalPriceOld = totalPriceOld.add(t.getPrice());
            }
            old.setTotalPrice(totalPriceOld);
            orderRepository.save(old);
            return this.orderMapper.orderToDetailedOrderDto(created);
        }
    }

    @Transactional
    @Override
    public DetailedOrderDto cancelTicketsOfOrder(DetailedOrderDto order) throws NotFoundException, ConflictException, ValidationException, BadCredentialsException {
        LOGGER.debug("cancelTicketsOfOrder ({})", order.getId());
        Order old = checkIfOrderExists(order.getId());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        authenticate(auth, old.getOrderBy().getId());
        List<DetailedTicketDto> ticketsToCancel = new ArrayList<>();
        List<DetailedTicketDto> ticketsToRemain = new ArrayList<>();
        if (old.getOrderType() == OrderType.BUY) {
            for (DetailedTicketDto t : order.getTickets()) {
                if (t.isReserved()) {
                    ticketsToCancel.add(t);
                } else {
                    ticketsToRemain.add(t);
                }
            }
        } else {
            for (DetailedTicketDto t : order.getTickets()) {
                if (!t.isReserved()) {
                    ticketsToCancel.add(t);
                } else {
                    ticketsToRemain.add(t);
                }
            }
        }
        simpleOrderValidator.validateOrderForCancellation(order, ticketsToCancel);
        if (ticketsToRemain.isEmpty()) {
            old.setOrderDate(LocalDateTime.now());
            old.setOrderType(OrderType.STORNO);
            for (Ticket t : old.getTickets()) {
                t.setCanceled(true);
                t.setReserved(false);
            }
            return orderMapper.orderToDetailedOrderDto(old);
        } else {
            BigDecimal totalPrice = BigDecimal.valueOf(0);
            for (DetailedTicketDto t : ticketsToCancel) {
                totalPrice = totalPrice.add(t.getPrice());
            }
            ApplicationUser forUser = userRepository.findUserById(order.getOrderBy().getId()).get(0);
            Set<DetailedTicketDto> set = new HashSet<>(ticketsToCancel);
            CreateOrderDto cancelation = new CreateOrderDto(LocalDateTime.now(), totalPrice, set, OrderType.STORNO, order.getStreet(),
                order.getCity(), order.getCountry(), order.getZip(), order.getPaymentType(), order.getNumberOfCard());
            Order toCreate = orderMapper.createOrderDtoToOrder(cancelation);
            toCreate.setOrderBy(forUser);
            toCreate.setOrderNumber(order.getOrderNumber());
            toCreate.setTickets(null);
            Order created = orderRepository.save(toCreate);
            for (DetailedTicketDto t : ticketsToCancel) {
                Ticket ticketEntity = ticketMapper.detailedTicketDtoToTicket(t);
                ticketEntity.setOrder(created);
                ticketEntity.setForSeat(this.seatMapper.seatDtoToSeat(t.getSeat()));
                ticketEntity.setReserved(false);
                ticketEntity.setCanceled(true);
                ticketRepository.save(ticketEntity);
            }
            BigDecimal totalPriceOld = BigDecimal.valueOf(0);
            for (DetailedTicketDto t : ticketsToRemain) {
                totalPriceOld = totalPriceOld.add(t.getPrice());
            }
            old.setTotalPrice(totalPriceOld);
            orderRepository.save(old);
            return this.orderMapper.orderToDetailedOrderDto(created);
        }
    }

    @Override
    public SimpleAddressDto getAddressOfLastOrder(UUID uid) throws NotFoundException, BadCredentialsException {
        LOGGER.debug("getAddressOfLastOrderFromUser ({})", uid);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        authenticate(auth, uid);
        List<ApplicationUser> forUsers = this.userRepository.findUserById(uid);
        if (forUsers.isEmpty()) {
            throw new NotFoundException(String.format("Could not find user with uid %s", uid));
        }
        List<Order> ordersOfUser = orderRepository.findOrdersWithTypeBuyForUserAndOrderByOrderDate(uid);
        Order lastOrder = null;
        SimpleAddressDto foundAddress = new SimpleAddressDto();
        if (!ordersOfUser.isEmpty()) {
            lastOrder = ordersOfUser.get(0);
            if (lastOrder.getStreet() != null && lastOrder.getCountry() != null && lastOrder.getCity() != null
                && lastOrder.getZip() != null) {
                foundAddress.setCity(lastOrder.getCity());
                foundAddress.setZip(lastOrder.getZip());
                foundAddress.setStreet(lastOrder.getStreet());
                foundAddress.setCountry(lastOrder.getCountry());
            }
        }
        return foundAddress;
    }

    private void authenticate(Authentication auth, UUID comparisonId) throws BadCredentialsException {
        LOGGER.trace("authenticate User with id ({})", comparisonId);
        if (auth == null) {
            throw new BadCredentialsException("You must be logged in access orders");
        } else if (
            auth.getAuthorities().stream().allMatch(aut -> aut.getAuthority().equals("ROLE_USER"))
                && !auth.getName().equals(comparisonId.toString())
        ) {
            throw new BadCredentialsException("You must be Admin to access orders of another user");
        }
    }

    private Order checkIfOrderExists(Long orderId) throws NotFoundException {
        LOGGER.trace("check order with id ({}) exists", orderId);
        Optional<Order> orders = orderRepository.findById(orderId);
        Order found;
        if (orders.isPresent()) {
            found = orders.get();
        } else {
            throw new NotFoundException(String.format("Could not find order with id %s", orderId));
        }
        return found;
    }

    private String generateOrderNumber(UUID userId) {
        LOGGER.trace("generate order number");
        String uuidString = userId.toString();
        String firstFourDigits = uuidString.substring(0, 4);
        Random random = new Random();
        int randomNumbers = random.nextInt(1001);
        String randomPart = Integer.toString(randomNumbers);
        LocalDateTime current = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddmmss");
        String timePart = current.format(formatter);
        return firstFourDigits + "-" + randomPart + "-" + timePart;
    }

}
