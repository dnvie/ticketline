package at.ac.tuwien.sepm.groupphase.backend.repository;

import at.ac.tuwien.sepm.groupphase.backend.entity.Sector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SectorRepository extends JpaRepository<Sector, UUID> {

    @Query("SELECT s FROM Sector s JOIN FETCH s.seats")
    List<Sector> findAllSectorWithSeats();

    @Query("SELECT s FROM Sector s JOIN FETCH s.seats WHERE s.id = :id")
    Optional<Sector> findSectorWithSeatsById(@Param("id") UUID id);
}
