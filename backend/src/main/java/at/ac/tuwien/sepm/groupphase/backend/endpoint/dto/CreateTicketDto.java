package at.ac.tuwien.sepm.groupphase.backend.endpoint.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateTicketDto {

    @NotNull(message = "Performance must not be null")
    private Long forPerformance;

    @NotNull(message = "Seat must not be null")
    private UUID forSeat;

    private boolean reserved;
    private boolean canceled;

    public CreateTicketDto(long l, UUID fromString, boolean b) {
    }
}
