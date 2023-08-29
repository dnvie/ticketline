package at.ac.tuwien.sepm.groupphase.backend.endpoint.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class DetailedCreateSeatDto extends CreateSeatDto {
    @NotNull(message = "Sector must not be null")
    @Valid
    private SimpleSectorDto sector;
}
