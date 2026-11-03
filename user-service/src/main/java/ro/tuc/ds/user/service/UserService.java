package ro.tuc.ds.user.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import ro.tuc.ds.user.client.AuthClient;
import ro.tuc.ds.user.client.DeviceClient;
import ro.tuc.ds.user.dto.UserCreateRequest;
import ro.tuc.ds.user.dto.UserResponse;
import ro.tuc.ds.user.dto.UserUpdateRequest;
import ro.tuc.ds.user.entity.Role;
import ro.tuc.ds.user.entity.User;
import ro.tuc.ds.user.exception.DuplicateResourceException;
import ro.tuc.ds.user.exception.ResourceNotFoundException;
import ro.tuc.ds.user.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private final UserRepository repository;
    private final DeviceClient deviceClient;
    private final AuthClient authClient;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UserService(UserRepository repository, DeviceClient deviceClient, AuthClient authClient) {
        this.repository = repository;
        this.deviceClient = deviceClient;
        this.authClient = authClient;
    }

    public List<UserResponse> findAll() {
        return repository.findAll().stream().map(UserResponse::from).toList();
    }

    public UserResponse findById(UUID id) {
        return UserResponse.from(getOrThrow(id));
    }

    public UserResponse create(UserCreateRequest req) {
        if (repository.existsByUsername(req.username())) {
            throw new DuplicateResourceException("Username already taken: " + req.username());
        }
        User u = new User();
        u.setUsername(req.username());
        u.setPassword(encoder.encode(req.password()));
        u.setName(req.name());
        u.setEmail(req.email());
        u.setRole(req.role());
        u = repository.save(u);
        try {
            authClient.syncCredential(u.getId(), u.getUsername(), req.password(), u.getRole());
        } catch (RuntimeException e) {
            // Without a credential the user could never log in: do not keep a half-created user.
            repository.deleteById(u.getId());
            throw e;
        }
        LOGGER.debug("User {} created", u.getId());
        return UserResponse.from(u);
    }

    public UserResponse update(UUID id, UserUpdateRequest req) {
        User u = getOrThrow(id);
        if (!u.getUsername().equals(req.username()) && repository.existsByUsername(req.username())) {
            throw new DuplicateResourceException("Username already taken: " + req.username());
        }
        u.setUsername(req.username());
        if (req.password() != null && !req.password().isBlank()) {
            u.setPassword(encoder.encode(req.password()));
        }
        u.setName(req.name());
        u.setEmail(req.email());
        u.setRole(req.role());
        u = repository.save(u);
        authClient.syncCredential(u.getId(), u.getUsername(), req.password(), u.getRole());
        return UserResponse.from(u);
    }

    public void delete(UUID id) {
        getOrThrow(id);
        authClient.deleteCredential(id);
        repository.deleteById(id);
        deviceClient.unassignAllDevicesOf(id);
        LOGGER.debug("User {} deleted", id);
    }

    /**
     * Makes sure the bootstrap administrator exists and can log in. Called at startup; safe to call repeatedly.
     * The password in the configuration is always the one that works.
     */
    public void ensureAdmin(String username, String password) {
        User u = repository.findByUsername(username).orElse(null);
        if (u == null) {
            create(new UserCreateRequest(username, password, "Administrator", "admin@ems.local", Role.ADMIN));
            return;
        }
        u.setRole(Role.ADMIN);
        u.setPassword(encoder.encode(password));
        u = repository.save(u);
        authClient.syncCredential(u.getId(), u.getUsername(), password, u.getRole());
    }

    private User getOrThrow(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));
    }
}
