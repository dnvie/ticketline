package at.ac.tuwien.sepm.groupphase.backend.service;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedMessageDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.MessageInquiryDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.Message;
import at.ac.tuwien.sepm.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.exception.ValidationException;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface MessageService {

    /**
     * Find all message entries ordered by published at date (descending).
     *
     * @param pageable is the given pageable with specified page and page size
     * @return ordered list of al message entries
     */
    List<Message> findAll(Pageable pageable);

    /**
     * Find all message entries ordered by published at date (descending), which have not been seen by the user with userId.
     *
     * @param userId the id of the user
     * @param pageable is the given pageable with specified page and page size
     * @return ordered list of all message entries which have not been seen by the user with userId
     */
    List<Message> findAllUnseen(UUID userId, Pageable pageable);


    /**
     * Find a single message entry by id.
     *
     * @param id the id of the message entry
     * @return the message entry
     */
    Message findOne(Long id, UUID userId);


    /**
     * Publish a single message entry.
     *
     * @param message to publish
     * @return published message entry
     */
    Message publishMessage(Message message);


    /**
     * Update a single message entry by id.
     *
     * @param id the id of the message entry
     * @param message the message entry
     * @return updated message entry
     *
     */
    DetailedMessageDto updateMessage(Long id, MessageInquiryDto message) throws NotFoundException, ValidationException, ConflictException;

    /**
     * Delete a single message entry by id.
     *
     * @param id of the message entry which should be deleted
     * @throws NotFoundException if no message with this id is found to delete
     */
    void deleteById(Long id) throws NotFoundException;

    /**
     * Get the total number of messages in the database.
     *
     * @return the total number of messages in the database
     */
    Long getTotalMessageCount();

    /**
     * Get the total number of unseen messages for a user.
     *
     * @param userId the id of the user
     * @return the total number of unseen messages for a user
     */
    Long getTotalUnseenMessageCount(UUID userId);
}
