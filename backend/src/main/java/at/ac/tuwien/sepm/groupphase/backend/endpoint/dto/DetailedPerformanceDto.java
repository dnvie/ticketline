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
public class DetailedPerformanceDto {
    private Long id;
    private String title;
    private SimpleLocationDto location;
    private SimpleEventDto event;
    private List<String> performers;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal price;
    private UUID seatMap;
}
