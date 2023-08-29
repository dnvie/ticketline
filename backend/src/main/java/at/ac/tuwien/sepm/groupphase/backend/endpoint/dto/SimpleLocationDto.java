package at.ac.tuwien.sepm.groupphase.backend.endpoint.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SimpleLocationDto {
    private Long id;

    @NotNull(message = "Name must not be null")
    @NotEmpty(message = "Name must not be empty")
    private String name;

    @NotNull(message = "Country must not be null")
    @NotEmpty(message = "Country must not be empty")
    @Size(max = 100, message = "Country must be between 1 and 100 characters")
    private String country;

    @NotNull(message = "City must not be null")
    @NotEmpty(message = "City must not be empty")
    @Size(max = 100, message = "City must be between 1 and 100 characters")
    private String city;


    @NotNull(message = "Postal code must not be null")
    @NotEmpty(message = "Postal code must not be empty")
    @Size(max = 20, message = "Postal code must be between 1 and 20 characters")
    private String postalCode;


    @NotNull(message = "Street must not be null")
    @NotEmpty(message = "Street must not be empty")
    @Size(max = 200, message = "Street must be between 1 and 200 characters")
    private String street;

    @Nullable
    private String description;
}
