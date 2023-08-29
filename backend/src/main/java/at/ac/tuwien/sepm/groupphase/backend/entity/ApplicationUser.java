package at.ac.tuwien.sepm.groupphase.backend.entity;

import com.github.javafaker.Faker;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Random;
import java.util.UUID;


@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder()
public class ApplicationUser {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    @Email
    @NotNull
    @NotEmpty
    @Column(unique = true)
    private String email;
    @NotNull
    @NotEmpty
    private String password;
    @NotNull
    private Boolean admin;
    @NotNull
    @NotEmpty
    private String firstName;
    @NotNull
    @NotEmpty
    private String lastName;
    @Nullable
    private String phoneNumber;
    @NotNull
    private Boolean enabled;
    @NotNull
    private Integer counter;

    public static ApplicationUser generateRandomUser() {
        Random random = new Random();
        Faker faker = new Faker();
        String email = faker.internet().emailAddress();
        String password = faker.internet().password(8, 14);
        boolean admin = random.nextDouble() <= 0.01;
        String first = faker.name().firstName();
        String last = faker.name().lastName();
        boolean enabled = random.nextDouble() <= 0.9;
        return new ApplicationUser(null, email, password, admin, first, last, null, enabled, 0);

    }
}
