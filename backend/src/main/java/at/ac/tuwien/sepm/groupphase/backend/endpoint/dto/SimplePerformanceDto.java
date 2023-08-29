package at.ac.tuwien.sepm.groupphase.backend.endpoint.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SimplePerformanceDto {
    Long id;
    String title;
    List<String> performers;
    SimpleLocationDto location;
    LocalDateTime startTime;
    LocalDateTime endTime;
    BigDecimal price;
    UUID seatMap;
}
