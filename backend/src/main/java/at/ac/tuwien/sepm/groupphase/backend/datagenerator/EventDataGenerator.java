package at.ac.tuwien.sepm.groupphase.backend.datagenerator;

import at.ac.tuwien.sepm.groupphase.backend.entity.Event;
import at.ac.tuwien.sepm.groupphase.backend.entity.Location;
import at.ac.tuwien.sepm.groupphase.backend.entity.Performance;
import at.ac.tuwien.sepm.groupphase.backend.entity.SeatMap;
import at.ac.tuwien.sepm.groupphase.backend.entity.enums.EventType;
import at.ac.tuwien.sepm.groupphase.backend.repository.EventRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.LocationRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.PerformanceRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.SeatMapRepository;
import io.micrometer.core.instrument.util.IOUtils;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Profile("generateData")
@Component
public class EventDataGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String TEST_EVENT_IMAGE = "data:image/png;base64,UklGRj4LAABXRUJQVlA4WAoAAAAgAAAApwQAyAIASUNDUMgBAAAAAAHIAAAAAAQwAABtbnRyUkdCIFhZWiAH4AABAAEAAAAAAABhY3NwAAAAAAAAAAAAAAAAAAAAAAAA\n"
        + "AAAAAAAAAAAAAQAA9tYAAQAAAADTLQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAlkZXNjAAAA8AAAACRyWFlaAAABFAAA\n"
        + "ABRnWFlaAAABKAAAABRiWFlaAAABPAAAABR3dHB0AAABUAAAABRyVFJDAAABZAAAAChnVFJDAAABZAAAAChiVFJDAAABZAAAAChjcHJ0AAABjAAAADxtbHVjAAAAAAAA\n"
        + "AAEAAAAMZW5VUwAAAAgAAAAcAHMAUgBHAEJYWVogAAAAAAAAb6IAADj1AAADkFhZWiAAAAAAAABimQAAt4UAABjaWFlaIAAAAAAAACSgAAAPhAAAts9YWVogAAAAAAAA\n"
        + "9tYAAQAAAADTLXBhcmEAAAAAAAQAAAACZmYAAPKnAAANWQAAE9AAAApbAAAAAAAAAABtbHVjAAAAAAAAAAEAAAAMZW5VUwAAACAAAAAcAEcAbwBvAGcAbABlACAASQBu\n"
        + "AGMALgAgADIAMAAxADZWUDggUAkAABDYAJ0BKqgEyQI+0WivUygmJCKg+PgRABoJaW7hd2Eas8+U7qyAwKyhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDh8o\n"
        + "omSglfJudQmSUBR+9JNzqEySgKP3pJudQmSUBR+9JNzqEySgKQLH6lBH2Dmn1Dhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4\n"
        + "cOHDhw4cOHyiiVeaexZ06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dO\n"
        + "nTp06dOnTp06dPPCBlWDmn1Dhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOH\n"
        + "Dhw4cOHDhw4cOHDhw+UUSrzT2LOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp\n"
        + "06dOnTp06dOnTp06dOnTqtNRVg5p9Q4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw\n"
        + "4cOHDhw4cOHDhw4cOHDhw4cOPYaiVeaexZ06dOnTpviZ5hmeuIkU+1fIPRfavkHovtXyD0UEru8qdA8IGVYOafUOHDhw4cOHDhw4cOHDhw4cOHDhwyNqoQpPUgerI3pv\n"
        + "pvpvpvpvpvppXyRy6njHZM6dOnTp06dOnTp06dOnTp06dOnTp06dOnTo6t0XFFdElSTl098n1GwteA9Junz/KprzNyEs6dOnTp06dOnTp06dVpqKsHNPqHDhw4cOG/Ms\n"
        + "GKg39hud5HDzOKHW2GrQEmobAIZkJP94Jm5CWdOnTp06dOnTp06dOnTp06dOnTp06dOnTpvufm+tCgkD+XgPwUrpMgKPCyeNhDOACf8OAE9Rh+O0zchLOnTp06dOnTp0\n"
        + "6dOnTp06dOnTp06dOnTp033Pzich3GO5IAHEVR0gtQ6BSC+rhmbkJZ06dOnTp06dOnTp06rTUVYOafUOHDhw4cN+ZYMVDxUbEAxGAYGcQHP+A9fAF6D0O+6Z+i9izp06\n"
        + "dOnTp06dOnT0FBlWDmn1Dhw4cOHDhkkNJH5OtnMYtdz3NMIx+Z+i9izp06dOnTp06dOnTp06dOnTp06dOnTp06b7n4wN5dEgVeAa/d/eFiFiFiFiFhb8xZQD7jG97FnT\n"
        + "p06dOnTp06dOnTp06dOnTp06dOnTp033Sy47JKSORIeMdkzp06dOnTp06dOnTp088IGVYOafUOHDhw4cMkjhELUi2LiMdkzp06dOnTp06dOnTp1XggZVg5p9Q4cOHDhw\n"
        + "35mVuVIG8ieLjHZM6dOnTp06dOnTp06dOnTp06dOnTp06dOnTo4GXpXd8Wq+Qei+1fIPRfavkHovtSEPiJZI+dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06duZFCbx5\n"
        + "6KbzT2LOnTp06dOnTp06eeEDKsHNPqHDhw4cOHEG187Sk0lxnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp\n"
        + "06dOnTp06dOnTp06dOnTp06dOnVaairBzT6hw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw\n"
        + "4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cew1Eq809izp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06d\n"
        + "OnTp06dOnTp06dOnTp06dOnTp06dOnTp088IGVYOafUOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cO\n"
        + "HDhw4cOHDhw4cOHDhw4cOHDhw4cOHDhw4cOHD5RRKvNPYs6dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dOnT\n"
        + "p06dOnTp06dOnTp06dOnTp06dOnTp06dOnTp06dHAAD+/7HU/6txBfIOabkX5RVb8oqt+UVW/KKrflFVvyiq35RVb8oqt+UVW/KKrflFVvyiq35RVb8oqt+UVW/KKrfl\n"
        + "FVvyiyNGDE4AAAABHgQAL2rAgAOVAAPkCAAAAAUgoABrgQAAAACkFAANcCAAAAAUgoABrgQAAByO9mPF4vF4vF4vECTYo28JdZ7z9LpIxhpb53Hw3i6J9LQMnOr5y5Od\n"
        + "Xzlyc6N1xf4i2ppKmEmBBNWDtqsOsEsteTDyH1dLaXxaXkKGkgdFfgOxl/G1dTOoPFF6ixjwATo/+itYwMSLfUN/6Hxv3y9BU0Y6XfVyRQcA8tqf4VVVaLE55pMkSzJe\n"
        + "i9lkf6vF81WiWlD1FBgReilqB1b7UQeQmv27zu16n+4W2XgfviE5G4rulHAX9ASmH9WrCQcX9vAkBAkgdsCXch4xcgHOPrT/cGz+28cJVxK50KXywNu/PEAWdrvhV7CZ\n"
        + "7mb+rfnOxmvFoPNrOpMeizVgIAJuC5IUBWB2Qmat3qfSy3xdUeGpQn/r6XruziNysotRaGXyTLPd5N0AT8fgo9BS1AgQv3bgCkrNl/HbGN2uTG/oCIboHX98vshNJ1Fb\n"
        + "ytiKi+wFNirwFsl3jzwvsBkXChsLAFHLrtatAyjQAjjy09P/nElDS1gFfVlB018N/eGlWKRIhxc623bbwnpbbwnphIooSjER4AmDZHLgqt8CAAjPAfouSLuHz09srq0X\n"
        + "gpar8s+O45gzV8BNYFxqHzLeJjKTQFOfxoL+6OopKNJerK8VRCgAHzLvF4vF4vF4vF2yIFKigADdAgAAAAFIKAAa4EAAAAApBQADXAgAAAAFIKAAa4EAAAAApBQADXAg\n"
        + "AAAAAAAA";

    private final EventRepository eventRepository;

    private final PerformanceRepository performanceRepository;

    private final LocationRepository locationRepository;

    private final LocationDataGenerator locationDataGenerator;

    private final SeatMapRepository seatMapRepository;

    private final Random random = new Random();

    public EventDataGenerator(EventRepository eventRepository, PerformanceRepository performanceRepository, LocationRepository locationRepository, LocationDataGenerator locationDataGenerator, SeatMapRepository seatMapRepository) {
        this.eventRepository = eventRepository;
        this.performanceRepository = performanceRepository;
        this.locationRepository = locationRepository;
        this.locationDataGenerator = locationDataGenerator;
        this.seatMapRepository = seatMapRepository;
    }

    public void generateEvent() throws FileNotFoundException {
        if (!eventRepository.findAll().isEmpty()) {
            LOGGER.debug("event already generated");
        } else {
            LOGGER.debug("generating event entries");
            List<Event> eventList = new ArrayList<>();
            for (int i = 0; i < 200; i++) {
                Event event = Event.generateRandomEvent();
                event.setImage(TEST_EVENT_IMAGE);
                eventList.add(event);
            }
            eventRepository.saveAll(eventList);
            generatePerformances();
            eventList = new ArrayList<>();
            eventList.add(generateRealEvent1());
            eventList.add(generateRealEvent2());
            eventList.add(generateRealEvent3());
            eventList.add(generateRealEvent4());
            eventList.add(generateRealEvent5());
            eventList.add(generateRealEvent6());
            eventList.add(generateRealEvent7());
            eventList.add(generateRealEvent8());
            eventList.add(generateRealEvent9());
            eventList.add(generateRealEvent10());
            eventList.add(generateRealEvent11());
            eventList.add(generateRealEvent12());
            eventRepository.saveAll(eventList);
        }
    }

    private void generatePerformances() {
        if (!performanceRepository.findAll().isEmpty()) {
            LOGGER.debug("performance already generated");
        } else {
            LOGGER.debug("generating performance entries");
            List<Event> eventList = eventRepository.findAll();
            List<Location> locationList = locationRepository.findAll();
            List<SeatMap> seatMapList = seatMapRepository.findAll();
            int seatMapSize = seatMapList.size();
            if (locationList.isEmpty()) {
                Location location = Location.generateRandomLocation();
                locationList.add(location);
                locationRepository.save(location);
            }
            for (Event event : eventList) {
                List<Performance> performanceList = new ArrayList<>();
                Random random = new Random();
                for (int i = 0; i < random.nextInt(1, 6); i++) {
                    int randomNumber = random.nextInt(seatMapSize);
                    Performance performance = Performance.generateRandomPerformance(event);
                    performance.setSeatMap(seatMapList.get(randomNumber));
                    performance.setLocation(locationList.get(random.nextInt(locationList.size())));
                    performanceList.add(performance);
                }
                performanceRepository.saveAll(performanceList);
            }
        }
    }

    private Event generateRealEvent1() throws FileNotFoundException {
        Event event = new Event();
        event.setTitle("Andrea Bocelli - Live in Concert");
        event.setType(EventType.CONCERT);
        event.setBeginDate(LocalDate.of(2023, 6, 28));
        event.setEndDate(LocalDate.of(2023, 6, 28));

        Performance performance1 = new Performance();
        performance1.setEvent(event);
        performance1.setTitle("Vorstellung Klagenfurt");
        performance1.setStartTime(LocalDateTime.of(2023, 6, 28, 20, 0));
        performance1.setEndTime(LocalDateTime.of(2023, 6, 28, 22, 0));
        performance1.setPrice(BigDecimal.valueOf(57.99));
        performance1.setSeatMap(getSeatmap());        //performance1.setSeatMap(seatMapRepository.findById().get());
        performance1.setLocation(locationRepository.findById(1L).get());
        Set<Performance> performances = new HashSet<>();
        performances.add(performance1);

        List<String> performers = new ArrayList<>();
        performers.add("Andrea Bocelli");
        performance1.setPerformers(performers);
        event.setPerformances(performances);

        FileInputStream fis = new FileInputStream("src/main/resources/testEvent1ImageBase64.txt");
        String image = IOUtils.toString(fis, StandardCharsets.UTF_8);
        event.setImage(image);

        return event;
    }

    private Event generateRealEvent2() throws FileNotFoundException {
        Event event = new Event();
        event.setTitle("Céline Dion - Courage World Tour");
        event.setType(EventType.CONCERT);
        event.setBeginDate(LocalDate.of(2023, 6, 29));
        event.setEndDate(LocalDate.of(2023, 6, 30));

        Performance performance1 = new Performance();
        performance1.setEvent(event);
        performance1.setTitle("Vorstellung Wien");
        performance1.setStartTime(LocalDateTime.of(2023, 6, 29, 17, 0));
        performance1.setEndTime(LocalDateTime.of(2023, 6, 29, 19, 30));
        performance1.setPrice(BigDecimal.valueOf(129.99));
        performance1.setSeatMap(getSeatmap());        //performance1.setSeatMap(seatMapRepository.findById().get());
        performance1.setLocation(locationRepository.findById(2L).get());

        List<String> performers = new ArrayList<>();
        performers.add("Céline Dion");
        performance1.setPerformers(performers);

        Set<Performance> performances = new HashSet<>();
        performances.add(performance1);

        Performance performance2 = new Performance();
        performance2.setEvent(event);
        performance2.setTitle("Vorstellung Zagreb");
        performance2.setStartTime(LocalDateTime.of(2023, 6, 30, 17, 0));
        performance2.setEndTime(LocalDateTime.of(2023, 6, 30, 19, 30));
        performance2.setPrice(BigDecimal.valueOf(129.99));
        performance2.setSeatMap(getSeatmap());        //performance2.setSeatMap(seatMapRepository.findById().get());
        performance2.setLocation(locationRepository.findById(2L).get());
        performances.add(performance2);
        performance2.setPerformers(performers);

        event.setPerformances(performances);

        FileInputStream fis = new FileInputStream("src/main/resources/testEvent2ImageBase64.txt");
        String image = IOUtils.toString(fis, StandardCharsets.UTF_8);
        event.setImage(image);

        return event;
    }

    private Event generateRealEvent3() throws FileNotFoundException {
        Event event = new Event();
        event.setTitle("Cirque du Soleil - OVO");
        event.setType(EventType.THEATER);
        event.setBeginDate(LocalDate.of(2023, 6, 30));
        event.setEndDate(LocalDate.of(2023, 6, 30));

        Performance performance1 = new Performance();
        performance1.setEvent(event);
        performance1.setTitle("Standardticket Wien");
        performance1.setStartTime(LocalDateTime.of(2023, 6, 30, 17, 0));
        performance1.setEndTime(LocalDateTime.of(2023, 6, 30, 19, 30));
        performance1.setPrice(BigDecimal.valueOf(29.99));
        performance1.setSeatMap(getSeatmap());        //performance1.setSeatMap(seatMapRepository.findById().get());
        performance1.setLocation(locationRepository.findById(3L).get());

        List<String> performers = new ArrayList<>();
        performers.add("Guy Laliberté");
        performers.add("Gilles Ste-Croix");
        performers.add("Deborah Colker");
        performers.add("Franco Dragone");
        performers.add("Daniel Gauthier");
        performers.add("Lyn Heward");
        performers.add("François Girard");
        performance1.setPerformers(performers);

        Set<Performance> performances = new HashSet<>();
        performances.add(performance1);

        Performance performance2 = new Performance();
        performance2.setEvent(event);
        performance2.setTitle("Exklusivticket Wien");
        performance2.setStartTime(LocalDateTime.of(2023, 6, 30, 17, 0));
        performance2.setEndTime(LocalDateTime.of(2023, 6, 30, 19, 30));
        performance2.setPrice(BigDecimal.valueOf(359.99));
        performance2.setSeatMap(getSeatmap());        //performance2.setSeatMap(seatMapRepository.findById().get());
        performance2.setLocation(locationRepository.findById(3L).get());
        performances.add(performance2);
        performance2.setPerformers(performers);

        event.setPerformances(performances);

        FileInputStream fis = new FileInputStream("src/main/resources/testEvent3ImageBase64.txt");
        String image = IOUtils.toString(fis, StandardCharsets.UTF_8);
        event.setImage(image);

        return event;
    }

    private Event generateRealEvent4() throws FileNotFoundException {
        Event event = new Event();
        event.setTitle("Cirque du Soleil - Luzia");
        event.setType(EventType.THEATER);
        event.setBeginDate(LocalDate.of(2023, 7, 1));
        event.setEndDate(LocalDate.of(2023, 7, 5));

        Performance performance1 = new Performance();
        performance1.setEvent(event);
        performance1.setTitle("Tageskarte Donnerstag");
        performance1.setStartTime(LocalDateTime.of(2023, 7, 1, 15, 0));
        performance1.setEndTime(LocalDateTime.of(2023, 7, 1, 20, 30));
        performance1.setPrice(BigDecimal.valueOf(39.99));
        performance1.setSeatMap(getSeatmap());        //performance1.setSeatMap(seatMapRepository.findById().get());
        performance1.setLocation(locationRepository.findById(4L).get());

        List<String> performers = new ArrayList<>();
        performers.add("Guy Laliberté");
        performers.add("Gilles Ste-Croix");
        performers.add("Deborah Colker");
        performers.add("Franco Dragone");
        performers.add("Daniel Gauthier");
        performers.add("Lyn Heward");
        performance1.setPerformers(performers);

        Set<Performance> performances = new HashSet<>();
        performances.add(performance1);

        Performance performance2 = new Performance();
        performance2.setEvent(event);
        performance2.setTitle("Tageskarte Freitag");
        performance2.setStartTime(LocalDateTime.of(2023, 7, 2, 17, 0));
        performance2.setEndTime(LocalDateTime.of(2023, 7, 2, 19, 30));
        performance2.setPrice(BigDecimal.valueOf(39.99));
        performance2.setSeatMap(getSeatmap());        //performance2.setSeatMap(seatMapRepository.findById().get());
        performance2.setLocation(locationRepository.findById(4L).get());
        performances.add(performance2);
        performance2.setPerformers(performers);

        Performance performance3 = new Performance();
        performance3.setEvent(event);
        performance3.setTitle("Tageskarte Montag");
        performance3.setStartTime(LocalDateTime.of(2023, 7, 5, 17, 0));
        performance3.setEndTime(LocalDateTime.of(2023, 7, 5, 19, 30));
        performance3.setPrice(BigDecimal.valueOf(39.99));
        performance3.setSeatMap(getSeatmap());        //performance3.setSeatMap(seatMapRepository.findById().get());
        performance3.setLocation(locationRepository.findById(4L).get());
        performances.add(performance3);
        performance3.setPerformers(performers);

        event.setPerformances(performances);

        FileInputStream fis = new FileInputStream("src/main/resources/testEvent4ImageBase64.txt");
        String image = IOUtils.toString(fis, StandardCharsets.UTF_8);
        event.setImage(image);

        return event;
    }

    private Event generateRealEvent5() throws FileNotFoundException {
        Event event = new Event();
        event.setTitle("Electric Love Festival 2023");
        event.setType(EventType.FESTIVAL);
        event.setBeginDate(LocalDate.of(2023, 7, 2));
        event.setEndDate(LocalDate.of(2023, 7, 4));

        Performance performance1 = new Performance();
        performance1.setEvent(event);
        performance1.setTitle("Tageskarte Freitag");
        performance1.setStartTime(LocalDateTime.of(2023, 7, 2, 17, 0));
        performance1.setEndTime(LocalDateTime.of(2023, 7, 2, 19, 30));
        performance1.setPrice(BigDecimal.valueOf(49.95));
        performance1.setSeatMap(getSeatmap());        //performance1.setSeatMap(seatMapRepository.findById().get());
        performance1.setLocation(locationRepository.findById(5L).get());

        List<String> performers = new ArrayList<>();
        performers.add("Armin van Buuren");
        performers.add("David Guetta");
        performers.add("Dimitri Vegas & Like Mike");
        performers.add("Marshmello");
        performers.add("Tiësto");
        performers.add("Timmy Trumpet");
        performers.add("Vini Vici");
        performers.add("W&W");
        performers.add("Afrojack");
        performers.add("Alan Walker");
        performers.add("Alesso");
        performers.add("Bassjackers");
        performers.add("Brennan Heart");
        performers.add("Da Tweekaz");
        performance1.setPerformers(performers);

        Set<Performance> performances = new HashSet<>();
        performances.add(performance1);

        Performance performance2 = new Performance();
        performance2.setEvent(event);
        performance2.setTitle("Tageskarte Samstag");
        performance2.setStartTime(LocalDateTime.of(2023, 7, 3, 17, 0));
        performance2.setEndTime(LocalDateTime.of(2023, 7, 3, 19, 30));
        performance2.setPrice(BigDecimal.valueOf(49.95));
        performance2.setSeatMap(getSeatmap());        //performance2.setSeatMap(seatMapRepository.findById().get());
        performance2.setLocation(locationRepository.findById(5L).get());
        performances.add(performance2);
        performance2.setPerformers(performers);

        Performance performance3 = new Performance();
        performance3.setEvent(event);
        performance3.setTitle("Tageskarte Sonntag");
        performance3.setStartTime(LocalDateTime.of(2023, 7, 4, 17, 0));
        performance3.setEndTime(LocalDateTime.of(2023, 7, 4, 19, 30));
        performance3.setPrice(BigDecimal.valueOf(49.95));
        performance3.setSeatMap(getSeatmap());        //performance3.setSeatMap(seatMapRepository.findById().get());
        performance3.setLocation(locationRepository.findById(5L).get());
        performances.add(performance3);
        performance3.setPerformers(performers);

        Performance performance4 = new Performance();
        performance4.setEvent(event);
        performance4.setTitle("VIP Tageskarte Sonntag");
        performance4.setStartTime(LocalDateTime.of(2023, 7, 4, 17, 0));
        performance4.setEndTime(LocalDateTime.of(2023, 7, 4, 19, 30));
        performance4.setPrice(BigDecimal.valueOf(399.95));
        performance4.setSeatMap(getSeatmap());        //performance4.setSeatMap(seatMapRepository.findById().get());
        performance4.setLocation(locationRepository.findById(5L).get());
        performances.add(performance4);
        performance4.setPerformers(performers);

        event.setPerformances(performances);

        FileInputStream fis = new FileInputStream("src/main/resources/testEvent5ImageBase64.txt");
        String image = IOUtils.toString(fis, StandardCharsets.UTF_8);
        event.setImage(image);

        return event;
    }

    private Event generateRealEvent6() throws FileNotFoundException {
        Event event = new Event();
        event.setTitle("FM4 Frequency 2023");
        event.setType(EventType.FESTIVAL);
        event.setBeginDate(LocalDate.of(2023, 7, 3));
        event.setEndDate(LocalDate.of(2023, 7, 5));

        Performance performance1 = new Performance();
        performance1.setEvent(event);
        performance1.setTitle("Tageskarte Freitag");
        performance1.setStartTime(LocalDateTime.of(2023, 7, 3, 17, 0));
        performance1.setEndTime(LocalDateTime.of(2023, 7, 3, 19, 30));
        performance1.setPrice(BigDecimal.valueOf(29.99));
        performance1.setSeatMap(getSeatmap());        //performance1.setSeatMap(seatMapRepository.findById().get());
        performance1.setLocation(locationRepository.findById(6L).get());

        List<String> performers = new ArrayList<>();
        performers.add("Imagine Dragons");
        performers.add("Die Ärzte");
        performers.add("Kraftklub");
        performers.add("The Killers");
        performers.add("The Lumineers");
        performers.add("The Offspring");
        performers.add("Twenty One Pilots");
        performers.add("Alligatoah");
        performers.add("Bausa");
        performers.add("Biffy Clyro");
        performers.add("Bilderbuch");
        performers.add("Black Veil Brides");
        performers.add("Bring Me The Horizon");
        performers.add("Casper");
        performers.add("Cypress Hill");
        performance1.setPerformers(performers);

        Set<Performance> performances = new HashSet<>();
        performances.add(performance1);

        Performance performance2 = new Performance();
        performance2.setEvent(event);
        performance2.setTitle("Tageskarte Samstag");
        performance2.setStartTime(LocalDateTime.of(2023, 7, 4, 17, 0));
        performance2.setEndTime(LocalDateTime.of(2023, 7, 4, 19, 30));
        performance2.setPrice(BigDecimal.valueOf(29.99));
        performance2.setSeatMap(getSeatmap());        //performance2.setSeatMap(seatMapRepository.findById().get());
        performance2.setLocation(locationRepository.findById(6L).get());
        performances.add(performance2);
        performance2.setPerformers(performers);

        Performance performance3 = new Performance();
        performance3.setEvent(event);
        performance3.setTitle("Tageskarte Sonntag");
        performance3.setStartTime(LocalDateTime.of(2023, 7, 5, 17, 0));
        performance3.setEndTime(LocalDateTime.of(2023, 7, 5, 19, 30));
        performance3.setPrice(BigDecimal.valueOf(29.99));
        performance3.setSeatMap(getSeatmap());        //performance3.setSeatMap(seatMapRepository.findById().get());
        performance3.setLocation(locationRepository.findById(6L).get());
        performances.add(performance3);
        performance3.setPerformers(performers);

        Performance performance4 = new Performance();
        performance4.setEvent(event);
        performance4.setTitle("VIP Festivalpass INKLUSIVE CAMPEN und PARKEN");
        performance4.setStartTime(LocalDateTime.of(2023, 7, 3, 17, 0));
        performance4.setEndTime(LocalDateTime.of(2023, 7, 5, 19, 30));
        performance4.setPrice(BigDecimal.valueOf(459.99));
        performance4.setSeatMap(getSeatmap());        //performance4.setSeatMap(seatMapRepository.findById().get());
        performance4.setLocation(locationRepository.findById(6L).get());
        performances.add(performance4);
        performance4.setPerformers(performers);

        event.setPerformances(performances);

        FileInputStream fis = new FileInputStream("src/main/resources/testEvent6ImageBase64.txt");
        String image = IOUtils.toString(fis, StandardCharsets.UTF_8);
        event.setImage(image);

        return event;
    }

    private Event generateRealEvent7() throws FileNotFoundException {
        Event event = new Event();
        event.setTitle("Hans Zimmer Live");
        event.setType(EventType.CONCERT);
        event.setBeginDate(LocalDate.of(2023, 7, 4));
        event.setEndDate(LocalDate.of(2023, 7, 4));

        Performance performance1 = new Performance();
        performance1.setEvent(event);
        performance1.setTitle("Aufführung Vormittag");
        performance1.setStartTime(LocalDateTime.of(2023, 7, 4, 10, 0));
        performance1.setEndTime(LocalDateTime.of(2023, 7, 4, 12, 30));
        performance1.setPrice(BigDecimal.valueOf(35.99));
        performance1.setSeatMap(getSeatmap());        //performance1.setSeatMap(seatMapRepository.findById().get());
        performance1.setLocation(locationRepository.findById(7L).get());

        List<String> performers = new ArrayList<>();
        performers.add("Hans Zimmer");
        performance1.setPerformers(performers);

        Set<Performance> performances = new HashSet<>();
        performances.add(performance1);

        Performance performance2 = new Performance();
        performance2.setEvent(event);
        performance2.setTitle("Aufführung Nachmittag");
        performance2.setStartTime(LocalDateTime.of(2023, 7, 4, 17, 0));
        performance2.setEndTime(LocalDateTime.of(2023, 7, 4, 19, 30));
        performance2.setPrice(BigDecimal.valueOf(35.99));
        performance2.setSeatMap(getSeatmap());        //performance2.setSeatMap(seatMapRepository.findById().get());
        performance2.setLocation(locationRepository.findById(7L).get());
        performances.add(performance2);
        performance2.setPerformers(performers);

        event.setPerformances(performances);

        FileInputStream fis = new FileInputStream("src/main/resources/testEvent7ImageBase64.txt");
        String image = IOUtils.toString(fis, StandardCharsets.UTF_8);
        event.setImage(image);

        return event;
    }

    private Event generateRealEvent8() throws FileNotFoundException {
        Event event = new Event();
        event.setTitle("Helene Fischer: Rausch - Live - Die Tour");
        event.setType(EventType.CONCERT);
        event.setBeginDate(LocalDate.of(2023, 7, 5));
        event.setEndDate(LocalDate.of(2023, 7, 6));

        Performance performance1 = new Performance();
        performance1.setEvent(event);
        performance1.setTitle("Aufführung Wien Montag");
        performance1.setStartTime(LocalDateTime.of(2023, 7, 5, 10, 0));
        performance1.setEndTime(LocalDateTime.of(2023, 7, 5, 15, 30));
        performance1.setPrice(BigDecimal.valueOf(45.99));
        performance1.setSeatMap(getSeatmap());        //performance1.setSeatMap(seatMapRepository.findById().get());
        performance1.setLocation(locationRepository.findById(8L).get());

        List<String> performers = new ArrayList<>();
        performers.add("Helene Fischer");
        performance1.setPerformers(performers);

        Set<Performance> performances = new HashSet<>();
        performances.add(performance1);

        Performance performance2 = new Performance();
        performance2.setEvent(event);
        performance2.setTitle("Aufführung Wien Dienstag");
        performance2.setStartTime(LocalDateTime.of(2023, 7, 6, 17, 0));
        performance2.setEndTime(LocalDateTime.of(2023, 7, 6, 19, 30));
        performance2.setPrice(BigDecimal.valueOf(45.99));
        performance2.setSeatMap(getSeatmap());        //performance2.setSeatMap(seatMapRepository.findById().get());
        performance2.setLocation(locationRepository.findById(8L).get());
        performances.add(performance2);
        performance2.setPerformers(performers);

        event.setPerformances(performances);

        FileInputStream fis = new FileInputStream("src/main/resources/testEvent8ImageBase64.txt");
        String image = IOUtils.toString(fis, StandardCharsets.UTF_8);
        event.setImage(image);

        return event;
    }

    private Event generateRealEvent9() throws FileNotFoundException {
        Event event = new Event();
        event.setTitle("Nova Rock 2023");
        event.setType(EventType.FESTIVAL);
        event.setBeginDate(LocalDate.of(2023, 7, 6));
        event.setEndDate(LocalDate.of(2023, 7, 8));

        Performance performance1 = new Performance();
        performance1.setEvent(event);
        performance1.setTitle("Tageskarte Dienstag");
        performance1.setStartTime(LocalDateTime.of(2023, 7, 6, 10, 0));
        performance1.setEndTime(LocalDateTime.of(2023, 7, 6, 16, 30));
        performance1.setPrice(BigDecimal.valueOf(105.99));
        performance1.setSeatMap(getSeatmap());        //performance1.setSeatMap(seatMapRepository.findById().get());
        performance1.setLocation(locationRepository.findById(9L).get());

        List<String> performers = new ArrayList<>();
        performers.add("Slipknot");
        performers.add("System of a Down");
        performers.add("The Prodigy");
        performers.add("Incubus");
        performers.add("The Offspring");
        performers.add("Rise Against");
        performers.add("Korn");
        performers.add("Limp Bizkit");
        performers.add("Marilyn Manson");
        performance1.setPerformers(performers);

        Set<Performance> performances = new HashSet<>();
        performances.add(performance1);

        Performance performance2 = new Performance();
        performance2.setEvent(event);
        performance2.setTitle("Tageskarte Mittwoch");
        performance2.setStartTime(LocalDateTime.of(2023, 7, 7, 17, 0));
        performance2.setEndTime(LocalDateTime.of(2023, 7, 7, 19, 30));
        performance2.setPrice(BigDecimal.valueOf(105.99));
        performance2.setSeatMap(getSeatmap());
        //performance2.setSeatMap(seatMapRepository.findById().get());
        performance2.setLocation(locationRepository.findById(9L).get());
        performances.add(performance2);
        performance2.setPerformers(performers);

        Performance performance3 = new Performance();
        performance3.setEvent(event);
        performance3.setTitle("Tageskarte Mittwoch");
        performance3.setStartTime(LocalDateTime.of(2023, 7, 7, 17, 0));
        performance3.setEndTime(LocalDateTime.of(2023, 7, 7, 19, 30));
        performance3.setPrice(BigDecimal.valueOf(105.99));
        performance3.setSeatMap(getSeatmap());
        //performance3.setSeatMap(seatMapRepository.findById().get());
        performance3.setLocation(locationRepository.findById(9L).get());
        performances.add(performance3);
        performance3.setPerformers(performers);

        Performance performance4 = new Performance();
        performance4.setEvent(event);
        performance4.setTitle("VIP Festivalpass");
        performance4.setStartTime(LocalDateTime.of(2023, 7, 6, 17, 0));
        performance4.setEndTime(LocalDateTime.of(2023, 7, 8, 19, 30));
        performance4.setPrice(BigDecimal.valueOf(525.99));
        performance4.setSeatMap(getSeatmap());        //performance4.setSeatMap(seatMapRepository.findById().get());
        performance4.setLocation(locationRepository.findById(9L).get());
        performances.add(performance4);
        performance4.setPerformers(performers);

        event.setPerformances(performances);

        FileInputStream fis = new FileInputStream("src/main/resources/testEvent9ImageBase64.txt");
        String image = IOUtils.toString(fis, StandardCharsets.UTF_8);
        event.setImage(image);

        return event;
    }

    private Event generateRealEvent10() throws FileNotFoundException {
        Event event = new Event();
        event.setTitle("Rammstein Europe Stadium Tour 2023");
        event.setType(EventType.CONCERT);
        event.setBeginDate(LocalDate.of(2023, 7, 8));
        event.setEndDate(LocalDate.of(2023, 7, 9));

        Performance performance1 = new Performance();
        performance1.setEvent(event);
        performance1.setTitle("Tageskarte Mittwoch");
        performance1.setStartTime(LocalDateTime.of(2023, 7, 8, 10, 0));
        performance1.setEndTime(LocalDateTime.of(2023, 7, 8, 12, 30));
        performance1.setPrice(BigDecimal.valueOf(55.98));
        performance1.setSeatMap(getSeatmap());
        //performance1.setSeatMap(seatMapRepository.findById().get());
        performance1.setLocation(locationRepository.findById(10L).get());

        List<String> performers = new ArrayList<>();
        performers.add("Rammstein");
        performance1.setPerformers(performers);

        Set<Performance> performances = new HashSet<>();
        performances.add(performance1);

        Performance performance2 = new Performance();
        performance2.setEvent(event);
        performance2.setTitle("Tageskarte Donnerstag");
        performance2.setStartTime(LocalDateTime.of(2023, 7, 9, 17, 0));
        performance2.setEndTime(LocalDateTime.of(2023, 7, 9, 19, 30));
        performance2.setPrice(BigDecimal.valueOf(35.99));
        performance2.setSeatMap(getSeatmap());
        //performance2.setSeatMap(seatMapRepository.findById().get());
        performance2.setLocation(locationRepository.findById(10L).get());
        performances.add(performance2);
        performance2.setPerformers(performers);

        event.setPerformances(performances);

        FileInputStream fis = new FileInputStream("src/main/resources/testEvent10ImageBase64.txt");
        String image = IOUtils.toString(fis, StandardCharsets.UTF_8);
        event.setImage(image);

        return event;
    }

    private Event generateRealEvent11() throws FileNotFoundException {
        Event event = new Event();
        event.setTitle("Sting - My Songs 2023");
        event.setType(EventType.CONCERT);
        event.setBeginDate(LocalDate.of(2023, 7, 10));
        event.setEndDate(LocalDate.of(2023, 7, 10));

        Performance performance1 = new Performance();
        performance1.setEvent(event);
        performance1.setTitle("Standard Ticket");
        performance1.setStartTime(LocalDateTime.of(2023, 7, 10, 10, 0));
        performance1.setEndTime(LocalDateTime.of(2023, 7, 10, 12, 30));
        performance1.setPrice(BigDecimal.valueOf(45.99));
        performance1.setSeatMap(getSeatmap());
        //performance1.setSeatMap(seatMapRepository.findById().get());
        performance1.setLocation(locationRepository.findById(11L).get());

        List<String> performers = new ArrayList<>();
        performers.add("Sting");
        performance1.setPerformers(performers);

        Set<Performance> performances = new HashSet<>();
        performances.add(performance1);

        Performance performance2 = new Performance();
        performance2.setEvent(event);
        performance2.setTitle("Exklusivticket");
        performance2.setStartTime(LocalDateTime.of(2023, 7, 10, 10, 0));
        performance2.setEndTime(LocalDateTime.of(2023, 7, 10, 12, 30));
        performance2.setPrice(BigDecimal.valueOf(45.99));
        performance2.setSeatMap(getSeatmap());
        //performance2.setSeatMap(seatMapRepository.findById().get());
        performance2.setLocation(locationRepository.findById(11L).get());
        performances.add(performance2);
        performance2.setPerformers(performers);

        event.setPerformances(performances);

        FileInputStream fis = new FileInputStream("src/main/resources/testEvent11ImageBase64.txt");
        String image = IOUtils.toString(fis, StandardCharsets.UTF_8);
        event.setImage(image);

        return event;
    }

    private Event generateRealEvent12() throws FileNotFoundException {
        Event event = new Event();
        event.setTitle("Volbeat 2023");
        event.setType(EventType.CONCERT);
        event.setBeginDate(LocalDate.of(2023, 7, 11));
        event.setEndDate(LocalDate.of(2023, 7, 14));

        Performance performance1 = new Performance();
        performance1.setEvent(event);
        performance1.setTitle("Ticket Wien");
        performance1.setStartTime(LocalDateTime.of(2023, 7, 11, 10, 0));
        performance1.setEndTime(LocalDateTime.of(2023, 7, 11, 12, 30));
        performance1.setPrice(BigDecimal.valueOf(65.95));
        performance1.setSeatMap(getSeatmap());
        //performance1.setSeatMap(seatMapRepository.findById().get());
        performance1.setLocation(locationRepository.findById(12L).get());

        List<String> performers = new ArrayList<>();
        performers.add("");
        performance1.setPerformers(performers);

        Set<Performance> performances = new HashSet<>();
        performances.add(performance1);

        Performance performance2 = new Performance();
        performance2.setEvent(event);
        performance2.setTitle("Ticket Graz");
        performance2.setStartTime(LocalDateTime.of(2023, 7, 12, 17, 0));
        performance2.setEndTime(LocalDateTime.of(2023, 7, 12, 19, 30));
        performance2.setPrice(BigDecimal.valueOf(65.95));
        performance2.setSeatMap(getSeatmap());
        //performance2.setSeatMap(seatMapRepository.findById().get());
        performance2.setLocation(locationRepository.findById(13L).get());
        performances.add(performance2);
        performance2.setPerformers(performers);

        Performance performance3 = new Performance();
        performance3.setEvent(event);
        performance3.setTitle("Ticket Salzburg");
        performance3.setStartTime(LocalDateTime.of(2023, 7, 13, 17, 0));
        performance3.setEndTime(LocalDateTime.of(2023, 7, 13, 19, 30));
        performance3.setPrice(BigDecimal.valueOf(65.95));
        performance3.setSeatMap(getSeatmap());
        //performance3.setSeatMap(seatMapRepository.findById().get());
        performance3.setLocation(locationRepository.findById(14L).get());
        performances.add(performance3);
        performance3.setPerformers(performers);

        Performance performance4 = new Performance();
        performance4.setEvent(event);
        performance4.setTitle("Ticket Vorarblberg");
        performance4.setStartTime(LocalDateTime.of(2023, 7, 14, 17, 0));
        performance4.setEndTime(LocalDateTime.of(2023, 7, 14, 19, 30));
        performance4.setPrice(BigDecimal.valueOf(65.95));
        performance4.setSeatMap(getSeatmap());
        //performance4.setSeatMap(seatMapRepository.findById().get());
        performance4.setLocation(locationRepository.findById(15L).get());
        performances.add(performance4);
        performance4.setPerformers(performers);

        event.setPerformances(performances);

        FileInputStream fis = new FileInputStream("src/main/resources/testEvent12ImageBase64.txt");
        String image = IOUtils.toString(fis, StandardCharsets.UTF_8);
        event.setImage(image);

        return event;
    }

    private SeatMap getSeatmap() {
        List<SeatMap> seatMapList = seatMapRepository.findAll();
        int seatMapSize = seatMapList.size();
        int randomNumber = random.nextInt(seatMapSize);
        return seatMapList.get(randomNumber);
    }
}
