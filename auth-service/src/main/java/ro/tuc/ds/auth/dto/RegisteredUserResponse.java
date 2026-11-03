package ro.tuc.ds.auth.dto;

import ro.tuc.ds.auth.entity.Role;

import java.util.UUID;

public record RegisteredUserResponse(UUID id, String username, String name, String email, Role role) {
}
