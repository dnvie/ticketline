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

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateDto {

    @NotNull(message = "Id must not be null")
    private UUID id;

    @NotNull(message = "Email must not be null")
    @Email
    private String email;

    @NotNull(message = "First name must not be null")
    private String firstName;

    @NotNull(message = "Last name must not be null")
    private String lastName;

    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Nullable
    private String password;

    @Nullable
    private String phoneNumber;

    @Nullable
    private Boolean admin;

    @Nullable
    private Boolean enabled;
}
