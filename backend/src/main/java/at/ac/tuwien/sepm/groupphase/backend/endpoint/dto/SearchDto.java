package at.ac.tuwien.sepm.groupphase.backend.endpoint.dto;

import at.ac.tuwien.sepm.groupphase.backend.entity.enums.EventType;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;


import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SearchDto {
    private String searchBar;
    private String street;
    private String city;
    private String country;
    private String zip;
    private EventType type;
    private LocalDate start;
    private LocalDate end;

    private LocalDate performanceDate;
    private LocalTime performanceTime;
    private Long minPrice;
    private Long maxPrice;
    private String eventName;
    private String roomName;
}
