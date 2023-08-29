package at.ac.tuwien.sepm.groupphase.backend.repository;

import at.ac.tuwien.sepm.groupphase.backend.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long>, JpaSpecificationExecutor<Location> {
    /**
     * Find all location entries with a certain name.
     *
     * @return ordered list of all location entries matching the given name
     */
    List<Location> findLocationByName(String name);

    /**
     * Find a location entry with a certain id.
     *
     * @return location entry matching the given id
     */
    Location findLocationById(Long id);


    @Query("SELECT DISTINCT l FROM Location l  WHERE lower(l.name) LIKE lower(concat('%',:name,'%'))")
    List<Location> findLocationsByNamePart(@Param("name") String name);


    /**
     * Delete a location entry by id.
     *
     * @param id the id of the location entry to delete
     * @return the count of deleted entities
     */
    Long deleteLocationById(Long id);
}
