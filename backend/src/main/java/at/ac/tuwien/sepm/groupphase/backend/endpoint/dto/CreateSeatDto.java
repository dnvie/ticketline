package at.ac.tuwien.sepm.groupphase.backend.endpoint.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class CreateSeatDto {
    @NotNull(message = "Number must not be null")
    private Integer number;
    @NotNull(message = "SeatRow must not be null")
    private Integer seatRow;
    @NotNull(message = "SeatColumn must not be null")
    private Integer seatColumn;
}
