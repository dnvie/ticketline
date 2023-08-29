package at.ac.tuwien.sepm.groupphase.backend.datagenerator;

import at.ac.tuwien.sepm.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepm.groupphase.backend.entity.Order;
import at.ac.tuwien.sepm.groupphase.backend.repository.OrderRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;


@Profile("generateData")
@Component
public class UserDataGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;
    private final OrderRepository orderRepository;

    public UserDataGenerator(UserRepository userRepository, PasswordEncoder passwordEncoder, OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.orderRepository = orderRepository;
    }

    public void generateUser() {
        if (!userRepository.findUserByEmail("admin@dickstoner.at").isEmpty()) {
            LOGGER.debug("user already generated");
        } else {
            LOGGER.debug("generating user entries");
            ApplicationUser user = new ApplicationUser();
            user.setEmail("admin@dickstoner.at");
            user.setPassword(passwordEncoder.encode("password"));
            user.setAdmin(true);
            user.setFirstName("dick");
            user.setLastName("stone");
            user.setEnabled(true);
            user.setCounter(0);
            userRepository.save(user);

            List<ApplicationUser> userList = new ArrayList<>();
            Set<String> uniqueEmails = new HashSet<>();
            for (int i = 0; i < 999; i++) {
                user = ApplicationUser.generateRandomUser();
                while (uniqueEmails.contains(user.getEmail())) {
                    user = ApplicationUser.generateRandomUser();
                }
                uniqueEmails.add(user.getEmail());
                userList.add(user);
            }
            userRepository.saveAll(userList);
        }

    }
}