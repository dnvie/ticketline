package at.ac.tuwien.sepm.groupphase.backend.endpoint.dto;

import at.ac.tuwien.sepm.groupphase.backend.entity.enums.OrderType;
import at.ac.tuwien.sepm.groupphase.backend.entity.enums.PaymentType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DetailedOrderDto {
    Long id;
    @NotNull(message = "order date must not be null")
    private LocalDateTime orderDate;
    @NotNull(message = "total price must not be null")
    private BigDecimal totalPrice;
    @NotNull(message = "user id must not be null")
    private SimpleUserDto orderBy;
    Set<DetailedTicketDto> tickets;
    private OrderType orderType;
    private String orderNumber;
    private String street;
    private String city;
    private String country;
    private String zip;
    private PaymentType paymentType;
    private String numberOfCard;
}
