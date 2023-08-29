package at.ac.tuwien.sepm.groupphase.backend.endpoint.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@AllArgsConstructor
@NoArgsConstructor
public class CreatePerformanceDto {
    @NotNull(message = "Title must not be null")
    @NotEmpty(message = "Title must not be empty")
    @Size(max = 100)
    String title;
    @NotNull(message = "Location must not be null")
    SimpleLocationDto location;
    @NotNull(message = "event must not be null")
    SimpleEventDto event;
    @NotNull(message = "Performers must not be null")
    @NotEmpty(message = "Performers must not be empty")
    @Size(max = 100)
    List<String> performers;
    @NotNull(message = "Start must not be null")
    LocalDateTime startTime;
    @NotNull(message = "End must not be null")
    LocalDateTime endTime;
    @NotNull(message = "Price must not be null")
    @DecimalMin(value = "0", inclusive = true)
    BigDecimal price;

    @Nullable
    UUID seatMap;
}
