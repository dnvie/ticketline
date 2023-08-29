package at.ac.tuwien.sepm.groupphase.backend.endpoint.dto;

import at.ac.tuwien.sepm.groupphase.backend.entity.enums.SectorType;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateSectorDto {
    @NotNull(message = "Type must not be null")
    private SectorType type;

    @NotNull(message = "Seats must not be null")
    @Size(min = 1, message = "Seats must not be empty")
    @Valid
    private Set<CreateSeatDto> seats;
    @NotNull(message = "Location must not be null")
    @NotEmpty(message = "Name must not be empty")
    @NotBlank(message = "Name must not be blank")
    private String name;

    @NotNull(message = "Price must not be null")
    @DecimalMin(value = "1.0", message = "Price factor for sector cannot be lower than 1.0")
    private BigDecimal price;

    @NotNull(message = "Orientation must not be null")
    private Integer orientation;

    @Nullable
    private Integer lodgeSize;

    @NotNull(message = "sectorRow must not be null")
    private Integer seatMapRow;

    @NotNull(message = "sectorColumn must not be null")
    private Integer seatMapColumn;

    @Nullable
    private Boolean noUpdate;

    @Nullable
    private Boolean standingSector;

    @NotNull(message = "Length must not be null")
    @Positive(message = "Width must be positive")
    private Integer length;

    @NotNull(message = "Width must not be null")
    @Positive(message = "Width must be positive")
    private Integer width;

}
