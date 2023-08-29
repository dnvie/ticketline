package at.ac.tuwien.sepm.groupphase.backend.basetest;

import at.ac.tuwien.sepm.groupphase.backend.entity.enums.EventType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public interface TestData {

    Long ID = 1L;

    UUID TEST_UUID = new UUID(0, 1);
    String TEST_NEWS_TITLE = "Title";
    String TEST_NEWS_SUMMARY = "Summary";
    String TEST_NEWS_TEXT = "TestMessageText";

    String TEST_EVENT_TITLE = "TestEventTitle";
    LocalDate TEST_EVENT_START_DATE = LocalDate.of(2020, 1, 1);
    LocalDate TEST_EVENT_END_DATE = LocalDate.of(2020, 1, 3);

    EventType TEST_EVENT_TYPE = EventType.FESTIVAL;

    String TEST_EVENT_IMAGE = "data:image/png;base64,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\n"
        + "AAAAAAAA";

    String TEST_LOCATION_NAME = "Test Location";

    String TEST_LOCATION_COUNTRY = "Test Country";

    String TEST_LOCATION_CITY = "Test city";

    String TEST_LOCATION_DESCRIPTION = "Test description";

    String TEST_LOCATION_POSTAL_CODE = "1234";

    String TEST_LOCATION_STREET = "Test Street 1/2";

    LocalDateTime TEST_NEWS_PUBLISHED_AT =
        LocalDateTime.of(2019, 11, 13, 12, 15, 0, 0);

    String TEST_USER_EMAIL = "TestUserEmail@email.com";
    String TEST_USER_FIRSTNAME = "TestUserFirstName";
    String TEST_USER_LASTNAME = "TestUserLastName";
    String TEST_USER_PASSWORD = "TestUserPassword";
    Boolean TEST_USER_ADMIN = false;
    Boolean TEST_USER_ENABLED = true;
    Integer TEST_USER_COUNTER = 0;


    String BASE_URI = "/api/v1";
    String MESSAGE_BASE_URI = BASE_URI + "/messages";
    String USER_BASE_URI = BASE_URI + "/users";

    String TICKET_BASE_URI = BASE_URI + "/tickets";

    String SEAT_BASE_URI = BASE_URI + "/seats";

    String SEATMAPS_SECTOR_BASE_URI = BASE_URI + "/seatmaps";

    String EVENT_BASE_URI = "/api/v1/events";

    String ADMIN_USER = "admin@email.com";
    List<String> ADMIN_ROLES = new ArrayList<>() {
        {
            add("ROLE_ADMIN");
            add("ROLE_USER");
        }
    };
    String DEFAULT_USER = "admin@email.com";

    String DEFAULT_USER_ID = "90857b96-a69b-466b-90de-08c0ea4e66f4";

    String DEFAULT_ADMIN_USER_ID = "ac55a452-f33d-42fe-9e85-185fc1f273ba";

    String DEFAULT_USER_PASSWORD = "password";
    List<String> USER_ROLES = new ArrayList<>() {
        {
            add("ROLE_USER");
        }
    };

}
