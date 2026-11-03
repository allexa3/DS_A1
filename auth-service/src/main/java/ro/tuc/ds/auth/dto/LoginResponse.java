package ro.tuc.ds.auth.dto;

import ro.tuc.ds.auth.entity.Role;

import java.util.UUID;

public record LoginResponse(String token, String tokenType, long expiresInSeconds,
                            UUID userId, String username, Role role) {
}
