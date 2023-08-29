package at.ac.tuwien.sepm.groupphase.backend.endpoint.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DetailedTicketDto {
    private Long id;

    @Nullable
    private BigDecimal price;
    @NotNull(message = "user id must not be null")
    private SimpleUserDto forUser;
    @NotNull(message = "performance must not be null")
    private DetailedPerformanceDto forPerformance;

    private SimpleSeatDto seat;

    private boolean reserved;
    private boolean canceled;
}
