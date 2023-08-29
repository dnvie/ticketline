package at.ac.tuwien.sepm.groupphase.backend.endpoint.dto;

import at.ac.tuwien.sepm.groupphase.backend.entity.enums.EventType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateEventDto {
    @NotNull(message = "Title must not be null")
    @NotEmpty(message = "Title must not be empty")
    @Size(max = 500, message = "Title must not be longer than 500 characters")
    private String title;

    @NotNull(message = "event type must not be null")
    private EventType type;

    @NotNull(message = "begin date must not be null")
    private LocalDate beginDate;

    @NotNull(message = "end date must not be null")
    private LocalDate endDate;

    @NotNull(message = "Image and Placeholder image are missing")
    private String image;

    private Set<CreatePerformanceDto> performances;
}
