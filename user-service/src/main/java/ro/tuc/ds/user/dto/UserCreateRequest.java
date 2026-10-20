package ro.tuc.ds.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ro.tuc.ds.user.entity.Role;

public record UserCreateRequest(
        @NotBlank String username,
        @NotBlank @Size(min = 6, message = "must have at least 6 characters") String password,
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotNull Role role) {
}
