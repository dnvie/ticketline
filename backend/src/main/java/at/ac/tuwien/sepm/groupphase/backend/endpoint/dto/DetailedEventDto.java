package at.ac.tuwien.sepm.groupphase.backend.endpoint.dto;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DetailedEventDto extends SimpleEventDto {

    @Valid
    private Set<SimplePerformanceDto> performances;
    private List<String> performers;
}
