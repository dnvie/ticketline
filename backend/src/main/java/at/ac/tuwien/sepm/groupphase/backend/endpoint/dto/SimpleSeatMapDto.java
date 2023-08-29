package at.ac.tuwien.sepm.groupphase.backend.endpoint.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SimpleSeatMapDto {
    private UUID id;
    private String name;
    private Long numberOfSectors;
    private Long numberOfSeats;
    private Boolean isUsed;
}
