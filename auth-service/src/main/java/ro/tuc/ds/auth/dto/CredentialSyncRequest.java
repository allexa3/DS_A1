package ro.tuc.ds.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ro.tuc.ds.auth.entity.Role;

/** Sent by the User service whenever a user is created or changed. A null/blank password keeps the current one. */
public record CredentialSyncRequest(@NotBlank String username, String password, @NotNull Role role) {
}
