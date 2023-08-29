package at.ac.tuwien.sepm.groupphase.backend.datagenerator;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.lang.invoke.MethodHandles;

@Profile("generateData")
@Component
public class DataGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final UserDataGenerator userDataGenerator;
    private final EventDataGenerator eventDataGenerator;
    private final LocationDataGenerator locationDataGenerator;
    private final MessageDataGenerator messageDataGenerator;
    private final OrderDataGenerator orderDataGenerator;
    private final SeatmapDataGenerator seatmapDataGenerator;

    public DataGenerator(UserDataGenerator userDataGenerator, EventDataGenerator eventDataGenerator, LocationDataGenerator locationDataGenerator,
                         MessageDataGenerator messageDataGenerator, OrderDataGenerator orderDataGenerator, SeatmapDataGenerator seatmapDataGenerator) {
        this.userDataGenerator = userDataGenerator;
        this.eventDataGenerator = eventDataGenerator;
        this.locationDataGenerator = locationDataGenerator;
        this.messageDataGenerator = messageDataGenerator;
        this.orderDataGenerator = orderDataGenerator;
        this.seatmapDataGenerator = seatmapDataGenerator;
    }

    @PostConstruct
    private void generateData() throws FileNotFoundException {
        LOGGER.debug("generating data");
        this.seatmapDataGenerator.generateSeatmaps();
        this.messageDataGenerator.generateMessage();
        this.userDataGenerator.generateUser();
        this.locationDataGenerator.generateLocation();
        this.eventDataGenerator.generateEvent();
        this.orderDataGenerator.generateOrders();
    }
}
