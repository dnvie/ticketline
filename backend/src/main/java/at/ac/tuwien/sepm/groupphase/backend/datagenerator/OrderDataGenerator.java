package at.ac.tuwien.sepm.groupphase.backend.datagenerator;

import at.ac.tuwien.sepm.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepm.groupphase.backend.entity.Order;
import at.ac.tuwien.sepm.groupphase.backend.entity.Performance;
import at.ac.tuwien.sepm.groupphase.backend.entity.Seat;
import at.ac.tuwien.sepm.groupphase.backend.entity.SeatMap;
import at.ac.tuwien.sepm.groupphase.backend.entity.Sector;
import at.ac.tuwien.sepm.groupphase.backend.entity.Ticket;
import at.ac.tuwien.sepm.groupphase.backend.repository.OrderRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.PerformanceRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.SeatMapRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.TicketRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;


@Profile("generateData")
@Component
public class OrderDataGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final OrderRepository orderRepository;

    private final UserRepository userRepository;

    private final PerformanceRepository performanceRepository;

    private final TicketRepository ticketRepository;

    private final SeatMapRepository seatMapRepository;

    public OrderDataGenerator(OrderRepository orderRepository, UserRepository userRepository, PerformanceRepository performanceRepository, TicketRepository ticketRepository, SeatMapRepository seatMapRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.performanceRepository = performanceRepository;
        this.ticketRepository = ticketRepository;
        this.seatMapRepository = seatMapRepository;
    }

    public void generateOrders() {
        if (!orderRepository.findAll().isEmpty()) {
            LOGGER.debug("orders already generated");
        } else {
            LOGGER.debug("generating order entries");
            List<ApplicationUser> allUsers = userRepository.findAll();
            if (!allUsers.isEmpty()) {
                Random random = new Random();
                for (int i = 0; i < 700; i++) {
                    int randomIndex = random.nextInt(allUsers.size());
                    if (allUsers.get(randomIndex).getFirstName().equals("dick") && allUsers.get(randomIndex).getLastName().equals("stone")) {
                        randomIndex = random.nextInt(allUsers.size());
                    }
                    at.ac.tuwien.sepm.groupphase.backend.entity.Order order = Order.generateRandomOrder(allUsers.get(randomIndex));
                    orderRepository.save(order);
                }
            }
            generateTicketsForOrders();
        }
    }

    private void generateTicketsForOrders() {
        LOGGER.debug("generating tickets for order entries");
        List<Order> allOrders = orderRepository.findAll();
        List<Performance> allPerformances = performanceRepository.findAll();
        if (!(allOrders.isEmpty() && allPerformances.isEmpty())) {
            Random random = new Random();
            for (Order o : allOrders) {
                int performanceIndex = random.nextInt(allPerformances.size());
                Performance p = allPerformances.get(performanceIndex);
                SeatMap seatMap = new SeatMap();
                List<Ticket> allTicketsOfPerformance = ticketRepository.findAllByPerformance(p.getId());
                List<Seat> soldSeats = new ArrayList<>();
                for (Ticket t : allTicketsOfPerformance) {
                    if (!t.isCanceled()) {
                        soldSeats.add(t.getForSeat());
                    }
                }
                Optional<SeatMap> seatMapOptional = seatMapRepository.findSeatMapByIdWithSectorAndSeats(p.getSeatMap().getId());
                if (seatMapOptional.isPresent()) {
                    seatMap = seatMapOptional.get();
                }
                int ticketCount = random.nextInt(10) + 1;
                for (int i = 0; i < ticketCount; i++) {
                    if (seatMap.getId() != null) {
                        Set<Seat> seats = new HashSet<>();
                        Set<Sector> sectors = seatMap.getSectors();
                        for (Sector s : sectors) {
                            seats.addAll(s.getSeats());
                        }
                        List<Seat> seatList = seats.stream().toList();
                        if (!seatList.isEmpty()) {
                            Seat selected;
                            do {
                                int randomSeat = random.nextInt(seatList.size());
                                selected = seatList.get(randomSeat);
                            } while (soldSeats.contains(selected));
                            Ticket ticket = Ticket.generateSingleTicket(p, o.getOrderBy(), selected, o);
                            ticketRepository.save(ticket);
                        } else {
                            Ticket ticket = Ticket.generateSingleTicket(p, o.getOrderBy(), null, o);
                            ticketRepository.save(ticket);
                        }
                    } else {
                        Ticket ticket = Ticket.generateSingleTicket(p, o.getOrderBy(), null, o);
                        ticketRepository.save(ticket);
                    }
                }
            }
        }

    }

}
