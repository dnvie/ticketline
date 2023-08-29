package at.ac.tuwien.sepm.groupphase.backend.repository;

import at.ac.tuwien.sepm.groupphase.backend.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;


@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    /**
     * Find all tickets entries for an order with a certain id.
     *
     * @return ordered list of all ticket entries matching the given id
     */
    @Query("SELECT t FROM Ticket t WHERE t.order.id = :id")
    Set<Ticket> findTicketsByOrderId(@Param("id") Long id);


    /**
     * Find all ticket entries for a user with a certain id.
     *
     * @param userId the id of the user
     * @return ordered list of all ticket entries matching the given id
     */
    @Query("SELECT t FROM Ticket t LEFT JOIN FETCH t.forUser "
        + "LEFT JOIN FETCH t.forPerformance LEFT JOIN FETCH t.forSeat WHERE t.forUser.id = :userId")
    List<Ticket> findAllByUserWithUserPerformanceAndSeat(@Param("userId") UUID userId);

    /**
     * Find all ticket entries for a performance with a certain id.
     *
     * @param performanceId the id of the performance
     * @return ordered list of all ticket entries matching the given id
     */
    @Query("SELECT t FROM Ticket t WHERE t.forPerformance.id = :performanceId AND t.canceled = false")
    List<Ticket> findAllByPerformance(@Param("performanceId") Long performanceId);

    /**
     * Find a ticket entry for a performance with a certain id and a seat with a certain id.
     *
     * @param performanceId the id of the performance
     * @param seatId        the id of the seat
     * @return the ticket entry matching the given ids
     */
    @Query("SELECT t FROM Ticket t WHERE t.forPerformance.id = :performanceId and t.forSeat.id = :seatId")
    List<Ticket> findTicketByPerformanceAndSeat(@Param("performanceId") Long performanceId,
                                                @Param("seatId") UUID seatId);

    /**
     * Find all ticket entries for an event with a certain id.
     *
     * @param eventId the id of the performance
     * @return the ticket entries matching the given ids
     */
    @Query("SELECT count(t) FROM Ticket t "
        + "WHERE t.forPerformance.event.id = :eventId")
    Long findTicketsOfEvent(@Param("eventId") Long eventId);

    /**
     * Delete a ticket entry by id.
     *
     * @param id the id of the ticket entry to delete
     * @return the count of deleted entities
     */
    Long deleteTicketById(Long id);
}
