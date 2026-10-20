package ro.tuc.ds.user.dto;

import ro.tuc.ds.user.entity.Role;
import ro.tuc.ds.user.entity.User;

import java.util.UUID;

public record UserResponse(UUID id, String username, String name, String email, Role role) {

    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getUsername(), u.getName(), u.getEmail(), u.getRole());
    }
}
