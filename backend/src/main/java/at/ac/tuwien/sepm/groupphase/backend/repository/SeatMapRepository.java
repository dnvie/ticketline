package at.ac.tuwien.sepm.groupphase.backend.repository;

import at.ac.tuwien.sepm.groupphase.backend.entity.SeatMap;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SeatMapRepository extends JpaRepository<SeatMap, UUID> {

    /**
     * Find seat map by id with its sectors and seats.
     *
     * @param id of the seat map to find
     * @return seat map with sectors and seats
     */
    @Query("SELECT s FROM SeatMap s LEFT JOIN FETCH s.sectors sec LEFT JOIN FETCH sec.seats WHERE s.id = :id")
    Optional<SeatMap> findSeatMapByIdWithSectorAndSeats(@Param("id") UUID id);

    /**
     * Find all seat maps with number of sectors and seats.
     *
     * @return list of all seat maps with number of sectors and seats
     */
    @Query("SELECT COUNT(Distinct(sec.id)) AS numberOfSectors, COUNT(Distinct(s.id)) AS numberOfSeats "
        + "FROM SeatMap sm LEFT JOIN Sector sec on sm.id = sec.seatMap.id LEFT JOIN Seat s on sec.id = s.sector.id GROUP BY sm.id")
    List<Object[]> findAllSeatMapsWithNumberOfSectorsAndSeats();

    /**
     * Find number of sectors and seats of specific seatMap by id.
     *
     * @param id of the seat map to find
     * @return seat map with number of sectors and seats
     */
    @Query("SELECT COUNT(Distinct(sec.id)) AS numberOfSectors, COUNT(Distinct(s.id)) AS numberOfSeats "
        + "FROM SeatMap sm LEFT JOIN Sector sec on sm.id = sec.seatMap.id LEFT JOIN Seat s on sec.id = s.sector.id WHERE sm.id = :id GROUP BY sm.id")
    List<Object[]> getNumberOfSectorsAndSeats(@Param("id") UUID id);

    /**
     * Find all seat maps with performances, sectors and seats.
     *
     * @return list of all seat maps with performances, sectors and seats
     */
    @Query("SELECT s FROM SeatMap s LEFT JOIN FETCH s.performance LEFT JOIN FETCH s.sectors sec LEFT JOIN FETCH sec.seats")
    List<SeatMap> findAllWithPerformancesSectorsAndSeats();

    /**
     * Find all seat maps with performances, sectors and seats with pagination.
     *
     * @param pageable the page information
     * @return list of all seat maps with performances, sectors and seats
     */
    @Query("SELECT s FROM SeatMap s LEFT JOIN FETCH s.performance LEFT JOIN FETCH s.sectors sec LEFT JOIN FETCH sec.seats")
    List<SeatMap> findAllWithPerformancesSectorsAndSeats(Pageable pageable);


    @Query("SELECT COUNT(s) FROM SeatMap s")
    Long countAllSeatMaps();
}
