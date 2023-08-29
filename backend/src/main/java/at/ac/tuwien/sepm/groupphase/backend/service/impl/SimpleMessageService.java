package at.ac.tuwien.sepm.groupphase.backend.service.impl;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedMessageDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.MessageInquiryDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper.MessageMapper;
import at.ac.tuwien.sepm.groupphase.backend.entity.Message;
import at.ac.tuwien.sepm.groupphase.backend.entity.SeenNews;
import at.ac.tuwien.sepm.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepm.groupphase.backend.repository.MessageRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepm.groupphase.backend.service.MessageService;
import at.ac.tuwien.sepm.groupphase.backend.repository.SeenNewsRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class SimpleMessageService implements MessageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;
    private final UserRepository userRepository;
    private final SeenNewsRepository seenNewsRepository;

    public SimpleMessageService(MessageRepository messageRepository, MessageMapper messageMapper, UserRepository userRepository, SeenNewsRepository seenNewsRepository) {
        this.messageRepository = messageRepository;
        this.messageMapper = messageMapper;
        this.userRepository = userRepository;
        this.seenNewsRepository = seenNewsRepository;
    }

    @Override
    public List<Message> findAll(Pageable pageable) {
        LOGGER.debug("Find all messages");
        return messageRepository.findAllByOrderByPublishedAtDesc(pageable);
    }

    @Override
    public List<Message> findAllUnseen(UUID userId, Pageable pageable) {
        LOGGER.debug("Find all unseen messages");
        var seenNewsIds = seenNewsRepository.findByUser_Id(userId).stream().map(SeenNews::getNews).map(Message::getId).toList();
        if (seenNewsIds.isEmpty()) {
            return messageRepository.findAllByOrderByPublishedAtDesc(pageable);
        }
        return messageRepository.findAllUnseen(seenNewsIds, pageable);
    }

    @Override
    public Message findOne(Long id, UUID userId) {
        LOGGER.debug("Find message with id {}", id);
        Optional<Message> message = messageRepository.findById(id);
        if (message.isPresent()) {
            if (userId != null) {
                SeenNews seenNews = new SeenNews();
                seenNews.setNews(message.get());
                seenNews.setUser(userRepository.findUserById((userId)).get(0));
                seenNewsRepository.save(seenNews);
            }
            return message.get();
        } else {
            throw new NotFoundException(String.format("Could not find message with id %s", id));
        }
    }

    @Override
    public Message publishMessage(Message message) {
        LOGGER.debug("Publish new message {}", message);
        message.setPublishedAt(LocalDateTime.now());
        return messageRepository.save(message);
    }

    public DetailedMessageDto updateMessage(Long id, MessageInquiryDto updatedMessage)
        throws NotFoundException, ValidationException, ConflictException {
        LOGGER.debug("Update message ({})", id);
        Optional<Message> message = messageRepository.findById(id);
        if (message.isEmpty()) {
            throw new NotFoundException(String.format("Could not find message with id %s", id));
        }
        Message toUpdate = message.get();
        Message newValues = messageMapper.messageInquiryDtoToMessage(updatedMessage);
        toUpdate.setTitle(newValues.getTitle());
        toUpdate.setSummary(newValues.getSummary());
        toUpdate.setText(newValues.getText());
        toUpdate.setPublishedAt(LocalDateTime.now());
        toUpdate.setImage(newValues.getImage());
        Message updated = messageRepository.save(toUpdate);
        deleteSeenNewsByNewsId(id);
        return messageMapper.messageToDetailedMessageDto(updated);
    }

    @Override
    public void deleteById(Long id) throws NotFoundException {
        LOGGER.debug("Delete message with id {}", id);
        Optional<Message> messageOptional = messageRepository.findById(id);
        if (messageOptional.isPresent()) {
            Message message = messageOptional.get();
            messageRepository.delete(message);
        } else {
            throw new NotFoundException(String.format("Could not find message with id %s", id));
        }
    }

    public void deleteSeenNewsByNewsId(Long userId) {
        seenNewsRepository.deleteAllByNewsId(userId);
    }

    public Long getTotalMessageCount() {
        return messageRepository.countAllMessages();
    }

    public Long getTotalUnseenMessageCount(UUID userId) {
        var seenNewsIds = seenNewsRepository.findByUser_Id(userId).stream().map(SeenNews::getNews).map(Message::getId).toList();
        if (seenNewsIds.isEmpty()) {
            return messageRepository.countAllMessages();
        }
        return (long) messageRepository.countAllUnseenMessages(seenNewsIds).size();
    }

}