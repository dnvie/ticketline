package at.ac.tuwien.sepm.groupphase.backend.repository;

import at.ac.tuwien.sepm.groupphase.backend.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface SeatRepository extends JpaRepository<Seat, UUID> {


    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Seat s WHERE s.sector.id = :sector_id AND s.number = :number")
    boolean existsByNumber(@Param("sector_id") UUID sectorId, @Param("number") Integer number);

    @Query("SELECT s FROM Seat s WHERE s.id = :id")
    Seat findByUuId(@Param("id") UUID id);
}
