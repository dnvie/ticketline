package at.ac.tuwien.sepm.groupphase.backend.repository;

import at.ac.tuwien.sepm.groupphase.backend.entity.ApplicationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<ApplicationUser, UUID> {

    /**
     * Find all users with given email.
     *
     * @param email the email of the users to find
     * @return ordered list all users with given email
     */
    List<ApplicationUser> findUserByEmail(String email);

    /**
     * Find all users that contain the given search string in their first or last name.
     *
     * @param userFirstName the search string for the first name
     * @param userLastName  the search string for the last name
     * @return ordered list all users with given email
     */
    List<ApplicationUser> findApplicationUserByEnabledIsFalseAndFirstNameContainingIgnoreCaseOrLastNameIsContainingIgnoreCase(String userFirstName, String userLastName);


    /**
     * Find all users that contain the given search string in their first or last name.
     *
     * @param userFirstName the search string for the first name
     * @param userLastName  the search string for the last name
     * @return ordered list all users with given email
     */
    List<ApplicationUser> findApplicationUserByFirstNameContainingIgnoreCaseOrLastNameIsContainingIgnoreCase(String userFirstName, String userLastName);

    /**
     * Delete users with given id.
     *
     * @param id the id of the users to delete
     * @return the number of users deleted
     */
    @Modifying
    @Query(value = "DELETE FROM ApplicationUser u WHERE u.id = ?1")
    int deleteByUuid(UUID id);


    /**
     * Find all users with given id.
     *
     * @param id the id of the users to find
     * @return ordered list all users with given id
     */
    List<ApplicationUser> findUserById(UUID id);

    List<ApplicationUser> findAllByEnabledIsFalse();
}
