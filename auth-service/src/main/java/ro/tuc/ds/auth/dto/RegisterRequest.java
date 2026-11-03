package ro.tuc.ds.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Self-registration: the new account always gets the CLIENT role. */
public record RegisterRequest(
        @NotBlank String username,
        @NotBlank @Size(min = 6, message = "must have at least 6 characters") String password,
        @NotBlank String name,
        @NotBlank @Email String email) {
}
