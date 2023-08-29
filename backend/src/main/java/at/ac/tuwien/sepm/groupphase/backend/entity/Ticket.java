package at.ac.tuwien.sepm.groupphase.backend.entity;

import at.ac.tuwien.sepm.groupphase.backend.entity.enums.OrderType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = true)
    private BigDecimal price;
    @ManyToOne()
    @JoinColumn(name = "performance_id")
    private Performance forPerformance;
    @ManyToOne()
    @JoinColumn(name = "user_id")
    private ApplicationUser forUser;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne()
    @JoinColumn(name = "seat_id")
    private Seat forSeat;

    private boolean reserved;
    private boolean canceled;

    public static Ticket generateSingleTicket(Performance p, ApplicationUser u, Seat s, Order o) {
        boolean reserved = false;
        boolean canceled = false;
        if (o.getOrderType() == OrderType.RESERVATION) {
            reserved = true;
        }
        if (o.getOrderType() == OrderType.STORNO) {
            reserved = false;
            canceled = true;
        }
        return new Ticket(null, BigDecimal.valueOf(10), p, u, o,  s, reserved, canceled);
    }
}
