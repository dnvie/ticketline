package at.ac.tuwien.sepm.groupphase.backend.endpoint;

import at.ac.tuwien.sepm.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedMessageDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.MessageInquiryDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.MessagesWithCountDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper.MessageMapper;
import at.ac.tuwien.sepm.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepm.groupphase.backend.service.MessageService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.lang.invoke.MethodHandles;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/messages")
public class MessageEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final MessageService messageService;
    private final MessageMapper messageMapper;
    private final SecurityProperties securityProperties;

    @Autowired
    public MessageEndpoint(MessageService messageService, MessageMapper messageMapper, SecurityProperties securityProperties) {
        this.messageService = messageService;
        this.messageMapper = messageMapper;
        this.securityProperties = securityProperties;
    }

    @PermitAll
    @GetMapping
    @Operation(summary = "Get list of messages without details")
    public MessagesWithCountDto findAll(@RequestParam(name = "showSeen", required = false) Boolean seen, @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "12") int size, @RequestHeader(name = "Authorization", required = false) String token) {
        LOGGER.info("GET /api/v1/messages");
        Pageable pageable = PageRequest.of(page, size);
        if (token == null || token.isEmpty()) {
            return new MessagesWithCountDto(messageMapper.messageToSimpleMessageDto(messageService.findAll(pageable)), messageService.getTotalMessageCount());
        } else {
            if (seen == null || !seen) {
                UUID userId = UUID.fromString(retrieveUserId(token));
                return new MessagesWithCountDto(messageMapper.messageToSimpleMessageDto(messageService.findAllUnseen(userId, pageable)), messageService.getTotalUnseenMessageCount(userId));
            } else {
                return new MessagesWithCountDto(messageMapper.messageToSimpleMessageDto(messageService.findAll(pageable)), messageService.getTotalMessageCount());
            }
        }
    }

    @PermitAll
    @GetMapping(value = "/{id}")
    @Operation(summary = "Get detailed information about a specific message")
    public DetailedMessageDto find(@PathVariable Long id, @RequestHeader(name = "Authorization", required = false) String token) {
        LOGGER.info("GET /api/v1/messages/{}", id);
        if (token == null || token.isEmpty()) {
            return messageMapper.messageToDetailedMessageDto(messageService.findOne(id, null));
        } else {
            UUID userId = UUID.fromString(retrieveUserId(token));
            return messageMapper.messageToDetailedMessageDto(messageService.findOne(id, userId));
        }
    }

    @Secured("ROLE_ADMIN")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    @Operation(summary = "Publish a new message", security = @SecurityRequirement(name = "apiKey"))
    public DetailedMessageDto create(@Valid @RequestBody MessageInquiryDto messageDto) {
        LOGGER.info("POST /api/v1/messages body: {}", messageDto);
        return messageMapper.messageToDetailedMessageDto(
            messageService.publishMessage(messageMapper.messageInquiryDtoToMessage(messageDto)));
    }

    @Secured("ROLE_ADMIN")
    @PutMapping(value = "/{id}")
    @PermitAll
    @ResponseStatus(HttpStatus.OK)
    public DetailedMessageDto updateEvent(@PathVariable("id") Long id, @Valid @RequestBody MessageInquiryDto messageDto)
        throws NotFoundException, IllegalArgumentException, ValidationException, ConflictException {
        LOGGER.info("PUT /api/v1/messages/{}", id);
        return messageService.updateMessage(id, messageDto);
    }

    @Secured("ROLE_ADMIN")
    @DeleteMapping(value = "/{id}")
    @PermitAll
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable("id") Long id) throws NotFoundException {
        LOGGER.info("DELETE /api/v1/messages/{}", id);
        messageService.deleteById(id);
    }

    private String retrieveUserId(String jwtToken) {
        byte[] signingKey = securityProperties.getJwtSecret().getBytes();
        Claims claims = Jwts.parserBuilder().setSigningKey(signingKey).build()
            .parseClaimsJws(jwtToken.replace(securityProperties.getAuthTokenPrefix(), ""))
            .getBody();
        String user = claims.getSubject();
        return user;
    }
}
