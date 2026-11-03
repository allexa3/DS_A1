package ro.tuc.ds.auth.security;

import ro.tuc.ds.auth.entity.Role;

import java.util.UUID;

public record AuthenticatedUser(UUID userId, String username, Role role) {
}
