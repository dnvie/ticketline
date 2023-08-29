package at.ac.tuwien.sepm.groupphase.backend.endpoint.dto;

import at.ac.tuwien.sepm.groupphase.backend.entity.enums.EventType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TopTenEventDto {

    private Long id;

    @NotNull(message = "Title must not be null")
    @NotEmpty(message = "Title must not be empty")
    @Size(max = 500, message = "Title must not be longer than 500 characters")
    private String title;

    @NotNull(message = "begin date must not be null")
    private LocalDate beginDate;

    @NotNull(message = "end date must not be null")
    private LocalDate endDate;

    @NotNull(message = "ticketCount must not be null")
    private Long ticketCount;
}
