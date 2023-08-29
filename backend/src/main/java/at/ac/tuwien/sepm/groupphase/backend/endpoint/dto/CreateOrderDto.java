package at.ac.tuwien.sepm.groupphase.backend.endpoint.dto;

import at.ac.tuwien.sepm.groupphase.backend.entity.enums.OrderType;
import at.ac.tuwien.sepm.groupphase.backend.entity.enums.PaymentType;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateOrderDto {
    private LocalDateTime orderDate;
    private BigDecimal totalPrice;
    Set<DetailedTicketDto> tickets;
    private OrderType orderType;
    private String street;
    private String city;
    private String country;
    private String zip;
    private PaymentType paymentType;
    private String numberOfCard;
}
