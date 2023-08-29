package at.ac.tuwien.sepm.groupphase.backend.repository;

import at.ac.tuwien.sepm.groupphase.backend.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Find all message entries ordered by published at date (descending).
     *
     * @param pageable is the given pageable with specified page and page size
     * @return ordered list of al message entries
     */
    List<Message> findAllByOrderByPublishedAtDesc(Pageable pageable);

    /**
     * Find all news entries with other ids than the given ordered by published at date (descending).
     *
     * @param ids list of news ids to exclude
     * @return ordered list of all news entries
     */
    @Query("select n from Message n where n.id not in :ids order by n.publishedAt DESC")
    List<Message> findAllUnseen(@Param("ids") Collection<Long> ids, Pageable pageable);

    /**
     * Get total number of unseen news for a user.
     *
     * @param ids list of news ids to exclude
     * @return list of ids of unseen news (to get the size of it)
     */
    @Query("select n from Message n where n.id not in :ids order by n.publishedAt DESC")
    List<Message> countAllUnseenMessages(@Param("ids") Collection<Long> ids);


    /**
     * Get total number of news entries.
     *
     * @return number of news entries
     */
    @Query("SELECT COUNT(m) FROM Message m")
    Long countAllMessages();



}
