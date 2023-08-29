package at.ac.tuwien.sepm.groupphase.backend.endpoint.dto;

import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SearchResultsDto {
    private List<String> artists;
    private List<SimpleLocationDto> locations;
    private List<DetailedEventDto> events;
    private List<DetailedPerformanceDto> performances;
}
