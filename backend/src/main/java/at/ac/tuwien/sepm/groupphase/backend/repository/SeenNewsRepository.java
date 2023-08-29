package at.ac.tuwien.sepm.groupphase.backend.repository;

import at.ac.tuwien.sepm.groupphase.backend.entity.SeenNews;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface SeenNewsRepository extends JpaRepository<SeenNews, Long> {

    /**
     * Finds all SeenNews entries for specific user with given id.
     *
     * @param id of the user
     * @return list of entries of SeenNews of the user
     */
    List<SeenNews> findByUser_Id(UUID id);

    /**
     * Deletes all SeenNews entries for specific news entry with given id.
     *
     * @param id of the news entry
     */
    void deleteAllByNewsId(Long id);

}
