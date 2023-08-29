package at.ac.tuwien.sepm.groupphase.backend.entity;

import at.ac.tuwien.sepm.groupphase.backend.entity.enums.OrderType;
import at.ac.tuwien.sepm.groupphase.backend.entity.enums.PaymentType;
import com.github.javafaker.Faker;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.Set;

@Entity
@Table(name = "orders")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private LocalDateTime orderDate;

    @Column(nullable = false)
    private BigDecimal totalPrice;
    @ManyToOne()
    @JoinColumn(name = "user_id")
    private ApplicationUser orderBy;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Ticket> tickets;
    @Column(nullable = false)
    private OrderType orderType;
    @Column(nullable = false)
    private String orderNumber;

    private String street;

    private String city;

    private String country;

    private String zip;

    private PaymentType paymentType;

    private String numberOfCard;

    public static Order generateRandomOrder(ApplicationUser user) {
        Faker faker = new Faker();
        Random random = new Random();
        OrderType randomType;
        int randomNumber = random.nextInt(100);
        if (randomNumber < 70) {
            randomType = OrderType.BUY;
        } else if (randomNumber < 90) {
            randomType = OrderType.RESERVATION;
        } else {
            randomType = OrderType.STORNO;
        }
        String orderNumber = faker.internet().password(8, 10);
        if (randomType == OrderType.BUY) {
            String street = faker.address().streetName();
            String country = faker.address().country();
            String city = faker.address().city();
            String zip = faker.address().zipCode();
            PaymentType type = PaymentType.banktransfer;
            String numberOfCard = faker.finance().iban();
            if (randomNumber < 50) {
                type = PaymentType.creditcard;
                numberOfCard = faker.finance().creditCard();
            }
            return new Order(null, LocalDateTime.now(), BigDecimal.valueOf(0), user, null, randomType, orderNumber, street, city, country, zip, type, numberOfCard);
        }
        return new Order(null, LocalDateTime.now(), BigDecimal.valueOf(0), user, null, randomType, orderNumber, null, null, null, null, null, null);

    }
}
