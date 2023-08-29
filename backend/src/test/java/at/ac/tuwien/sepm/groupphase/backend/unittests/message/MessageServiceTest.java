package at.ac.tuwien.sepm.groupphase.backend.unittests.message;

import at.ac.tuwien.sepm.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.MessageInquiryDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.Message;
import at.ac.tuwien.sepm.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import at.ac.tuwien.sepm.groupphase.backend.service.MessageService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.UUID;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "classpath:sql/insertMessageTestData.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class MessageServiceTest implements TestData {
    @Autowired
    private MessageService messageService;

    @Test
    public void givenNothing_whenFindAll_thenFindListWithAllEvents() {
        Pageable pageable = Pageable.ofSize(12);
        List<Message> messages = messageService.findAll(pageable);

        assertEquals(10, messages.size());
        assertThat(messages)
            .map(Message::getId, Message::getTitle)
            .contains(org.assertj.core.api.Assertions.tuple(-1L, "testTitle1"))
            .contains(org.assertj.core.api.Assertions.tuple(-2L, "testTitle2"))
            .contains(org.assertj.core.api.Assertions.tuple(-3L, "testTitle3"))
            .contains(org.assertj.core.api.Assertions.tuple(-4L, "testTitle4"))
            .contains(org.assertj.core.api.Assertions.tuple(-5L, "testTitle5"))
            .contains(org.assertj.core.api.Assertions.tuple(-6L, "testTitle6"))
            .contains(org.assertj.core.api.Assertions.tuple(-7L, "testTitle7"))
            .contains(org.assertj.core.api.Assertions.tuple(-8L, "testTitle8"))
            .contains(org.assertj.core.api.Assertions.tuple(-9L, "testTitle9"))
            .contains(org.assertj.core.api.Assertions.tuple(-10L, "testTitle10"));
    }

    @Test
    public void requestedDetailedMessageShouldContainAllAttributes() {
        Message message = messageService.findOne(-1L, null);

        assertAll(
            () -> assertEquals(-1L, message.getId()),
            () -> assertEquals("testTitle1", message.getTitle()),
            () -> assertEquals("testSummary1", message.getSummary()),
            () -> assertEquals("testText1", message.getText()),
            () -> assertEquals("2023-06-25T12:00", message.getPublishedAt().toString())
        );
    }

    @Test
    public void createValidMessageShouldReturnMessage() {

        Message message = new Message();
        message.setId(-11L);
        message.setTitle("testTitle11");
        message.setSummary("testSummary11");
        message.setText("testText11");

        assertAll(
            () -> assertEquals("testTitle11", message.getTitle()),
            () -> assertEquals("testSummary11", message.getSummary()),
            () -> assertEquals("testText11", message.getText())
        );
    }

    @Test
    public void getUnseenNewsShouldOnlyReturnUnseenNews() {
        Pageable pageable = Pageable.ofSize(12);
        List<Message> unseenMessages = messageService.findAllUnseen(UUID.fromString("ac55a452-f33d-42fe-9e85-185fc1f273ba"), pageable);

        assertEquals(4, unseenMessages.size());
        assertThat(unseenMessages)
            .map(Message::getId, Message::getTitle)
            .contains(org.assertj.core.api.Assertions.tuple(-7L, "testTitle7"))
            .contains(org.assertj.core.api.Assertions.tuple(-8L, "testTitle8"))
            .contains(org.assertj.core.api.Assertions.tuple(-9L, "testTitle9"))
            .contains(org.assertj.core.api.Assertions.tuple(-10L, "testTitle10"));

    }

    @Test
    public void newsEntryShouldBeAddedToSeenNewsAfterLoggedInUserViewsNewsDetails() {
        Message message = messageService.findOne(-7L, UUID.fromString("ac55a452-f33d-42fe-9e85-185fc1f273ba"));

        Pageable pageable = Pageable.ofSize(10);
        List<Message> unseenMessages = messageService.findAllUnseen(UUID.fromString("ac55a452-f33d-42fe-9e85-185fc1f273ba"), pageable);

        assertEquals(3, unseenMessages.size());
        assertThat(unseenMessages)
            .map(Message::getId, Message::getTitle)
            .contains(org.assertj.core.api.Assertions.tuple(-8L, "testTitle8"))
            .contains(org.assertj.core.api.Assertions.tuple(-9L, "testTitle9"))
            .contains(org.assertj.core.api.Assertions.tuple(-10L, "testTitle10"))
            .doesNotContain(org.assertj.core.api.Assertions.tuple(-7L, "testTitle7"));
    }

    @Test
    public void deleteMessageWithValidIdShouldDeleteMessage() {
        messageService.deleteById(-1L);
        Pageable pageable = Pageable.ofSize(9);
        List<Message> messages = messageService.findAll(pageable);
        assertEquals(9, messages.size());
        assertThat(messages)
            .map(Message::getId, Message::getTitle)
            .contains(org.assertj.core.api.Assertions.tuple(-2L, "testTitle2"))
            .contains(org.assertj.core.api.Assertions.tuple(-3L, "testTitle3"))
            .contains(org.assertj.core.api.Assertions.tuple(-4L, "testTitle4"))
            .contains(org.assertj.core.api.Assertions.tuple(-5L, "testTitle5"))
            .contains(org.assertj.core.api.Assertions.tuple(-6L, "testTitle6"))
            .contains(org.assertj.core.api.Assertions.tuple(-7L, "testTitle7"))
            .contains(org.assertj.core.api.Assertions.tuple(-8L, "testTitle8"))
            .contains(org.assertj.core.api.Assertions.tuple(-9L, "testTitle9"))
            .contains(org.assertj.core.api.Assertions.tuple(-10L, "testTitle10"));
    }

    @Test
    public void deleteMessageWithInvalidIdShouldThrowException() {
        NotFoundException exception = assertThrows(NotFoundException.class, () -> messageService.deleteById(-123L));
        assertTrue(exception.getMessage().contains("Could not find message with id -123"));
    }

    @Test
    public void updateMessageWithValidIdShouldUpdateMessage() throws ValidationException, ConflictException {
        MessageInquiryDto newMessage = new MessageInquiryDto();
        newMessage.setTitle("updatedTitle");
        newMessage.setSummary("updatedSummary");
        newMessage.setText("updatedText");
        newMessage.setImage("1");
        messageService.updateMessage(-10L, newMessage);
        Message message = messageService.findOne(-10L, null);

        assertAll(
            () -> assertEquals("updatedTitle", message.getTitle()),
            () -> assertEquals("updatedSummary", message.getSummary()),
            () -> assertEquals("updatedText", message.getText())
        );
    }
}
