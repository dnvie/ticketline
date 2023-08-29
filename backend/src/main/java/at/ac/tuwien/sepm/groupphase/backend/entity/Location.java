package at.ac.tuwien.sepm.groupphase.backend.entity;

import com.github.javafaker.Faker;
import jakarta.annotation.Nullable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String country;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 20)
    private String postalCode;

    @Column(nullable = false, length = 200)
    private String street;

    @Nullable
    private String description;

    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL)
    private Set<Performance> performances;

    public static Location generateRandomLocation() {
        Faker faker = new Faker();
        String name = faker.company().name();
        String country = faker.address().country();
        String city = faker.address().city();
        String postalCode = faker.address().zipCode();
        String street = faker.address().streetAddress();
        String description = faker.lorem().sentence();

        return new Location(null, name, country, city, postalCode, street, description, new HashSet<>());

    }
}
