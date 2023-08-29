package at.ac.tuwien.sepm.groupphase.backend.repository;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.TopTenEventDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.Event;
import at.ac.tuwien.sepm.groupphase.backend.entity.enums.EventType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    /**
     * Find all event entries with a certain title.
     *
     * @return ordered list of all event entries matching the given name
     */
    List<Event> findEventByTitle(String title);

    /**
     * Find all event entries with a certain title.
     *
     * @param title the title to search for
     * @return ordered list of all event entries matching the given name
     */
    List<Event> findAllByTitleContainingIgnoreCase(String title);

    /**
     * Find an event entry with a certain id.
     *
     * @return event entry matching the given id
     */
    Event findEventById(Long id);


    /**
     * Delete an event entry by id.
     *
     * @param id the id of the event entry to delete
     * @return the count of deleted entities
     */
    Long deleteEventById(Long id);

    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.performances p LEFT JOIN FETCH p.location WHERE e.id = :id")
    Optional<Event> findByIdWithPerformancesAndLocation(@Param("id") Long id);

    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.performances p LEFT JOIN FETCH p.location ORDER BY e.beginDate ASC")
    List<Event> findAllWithPerformancesAndLocation(Pageable pageable);

    @Query("SELECT DISTINCT p.performers FROM Performance p JOIN p.event e WHERE e.id = :id")
    List<String> findAllPerformers(@Param("id") Long id);


    @Query("SELECT DISTINCT performer FROM Performance p JOIN p.performers performer WHERE lower(performer) LIKE lower(concat('%',:name,'%'))")
    List<String> findAllPerformersByName(@Param("name") String name);

    @Query("SELECT e FROM Event e "
        + "LEFT JOIN FETCH e.performances p "
        + "LEFT JOIN FETCH p.performers performer WHERE performer = :name")
    List<Event> findEventsMatchingToPerformerName(@Param("name") String name);

    @Query("SELECT e FROM Event e "
        + "LEFT JOIN FETCH e.performances p "
        + "LEFT JOIN FETCH p.location location WHERE location.name = :name")
    List<Event> findEventsMatchingToLocation(@Param("name") String name);

    @Query("SELECT e FROM Event e WHERE lower(e.title)  LIKE lower(concat('%',:name,'%')) ")
    List<Event> findEventsByPartOfTitle(@Param("name") String name);

    @Query("SELECT new at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.TopTenEventDto(e.id, e.title, e.beginDate, e.endDate, COUNT(t)) "
        + "FROM Event e "
        + "JOIN e.performances p "
        + "JOIN Ticket t ON t.forPerformance.id = p.id "
        + "WHERE FUNCTION('YEAR', t.order.orderDate) = :year "
        + "AND FUNCTION('MONTH', t.order.orderDate) = :month "
        + "AND (e.type = :type OR :type IS NULL) "
        + "GROUP BY e.id, e.title, e.type, e.beginDate, e.endDate, e.image "
        + "ORDER BY COUNT(t) DESC")
    List<TopTenEventDto> findTopEventsByTicketSalesPerMonth(@Param("year") int year, @Param("month") int month, @Param("type") EventType type, Pageable pageable);

    @Query("SELECT COUNT(e) FROM Event e")
    Long countAllEvents();
}
