package at.ac.tuwien.sepm.groupphase.backend.entity;

import at.ac.tuwien.sepm.groupphase.backend.entity.enums.EventType;
import com.github.javafaker.Faker;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Performance {
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "location_id")
    Location location;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 500)
    private String title;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "event_id")
    private Event event;
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> performers = new ArrayList<>();

    @Column(nullable = false, name = "start_time")
    private LocalDateTime startTime;
    @Column(nullable = false, name = "end_time")
    private LocalDateTime endTime;

    @Column(nullable = false)
    @DecimalMin(value = "0", inclusive = true)
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "seat_map_id")
    private SeatMap seatMap;

    public static Performance generateRandomPerformance(Event event) {
        Random random = new Random();
        Faker faker = new Faker();
        String title = faker.lorem().word() + " " + faker.lorem().word();
        List<String> performers = new ArrayList<>();
        for (int i = 0; i < random.nextInt(1, 8); i++) {
            performers.add(faker.artist().name());
        }
        LocalDateTime startTime;
        LocalDateTime endTime;
        if (event != null && event.getBeginDate() != null && event.getEndDate() != null) {
            int duration = event.getEndDate().getDayOfYear() - event.getBeginDate().getDayOfYear();
            startTime = event.getBeginDate().atStartOfDay().plusDays(random.nextInt(Math.abs(duration)));
            endTime = startTime.plusHours(random.nextInt(3) + 1);
        } else {
            startTime = LocalDateTime.now().plusDays(random.nextInt(100));
            endTime = startTime.plusHours(random.nextInt(3) + 1);
        }
        BigDecimal price = BigDecimal.valueOf(random.nextInt(1000) + 1000, 2);

        return new Performance(null, null, title, event, performers, startTime, endTime, price, null);

    }
}
