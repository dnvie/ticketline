package at.ac.tuwien.sepm.groupphase.backend.endpoint.dto;

import at.ac.tuwien.sepm.groupphase.backend.entity.enums.SectorType;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class SimpleSectorDto {
    private UUID id;

    @NotNull(message = "Type must not be null")
    private SectorType type;

    @NotNull(message = "Name must not be null")
    @NotEmpty(message = "Name must not be empty")
    @NotBlank(message = "Name must not be blank")
    private String name;

    @NotNull(message = "Price must not be null")
    private BigDecimal price;

    @NotNull(message = "Orientation must not be null")
    private Integer orientation;

    @Nullable
    private Integer lodgeSize;

    @NotNull(message = "Sector row must not be null")
    private Integer seatMapRow;

    @NotNull(message = "Sector column must not be null")
    private Integer seatMapColumn;

    @Nullable
    private Boolean noUpdate;

    @Nullable
    private Boolean standingSector;

    @NotNull(message = "Length must not be null")
    @Positive(message = "Length must be positive")
    private Integer length;

    @NotNull(message = "Width must not be null")
    @Positive(message = "Width must be positive")
    private Integer width;

}
