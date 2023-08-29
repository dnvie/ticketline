package at.ac.tuwien.sepm.groupphase.backend.endpoint.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DetailedSeatDto {

    private UUID id;

    @NotNull(message = "Number must not be null")
    private Integer number;

    @NotNull(message = "Sector must not be null")
    private SimpleSectorDto sector;

    @NotNull(message = "Row must not be null")
    private Integer seatRow;

    @NotNull(message = "Column must not be null")
    private Integer seatColumn;
}
