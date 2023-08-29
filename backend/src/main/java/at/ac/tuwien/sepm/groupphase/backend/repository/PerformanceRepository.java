package at.ac.tuwien.sepm.groupphase.backend.repository;


import at.ac.tuwien.sepm.groupphase.backend.entity.Performance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PerformanceRepository extends JpaRepository<Performance, Long> {
    /**
     * Find all performance entries.
     *
     * @return list of all performance entries
     */
    List<Performance> findAll();

    @Query("SELECT DISTINCT p FROM Performance p WHERE lower(p.title) LIKE lower(concat('%',:name,'%'))")
    List<Performance> findPerformancesByNamePart(@Param("name") String name);

    @Query("SELECT p FROM Performance p WHERE p.id = :id")
    Performance findPerformanceById(@Param("id") Long id);

}
