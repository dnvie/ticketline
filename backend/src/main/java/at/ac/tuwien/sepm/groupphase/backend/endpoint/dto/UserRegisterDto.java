package at.ac.tuwien.sepm.groupphase.backend.endpoint.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegisterDto {

    @NotNull(message = "Email must not be null")
    @Email
    private String email;

    @Size(min = 8, message = "Password must be at least 8 characters long")
    @NotNull(message = "Password must not be null")
    private String password;

    @NotNull(message = "First name must not be null")
    private String firstName;

    @NotNull(message = "Last name must not be null")
    private String lastName;

    @Nullable
    private String phoneNumber;

    @Nullable
    private Boolean admin;

}
