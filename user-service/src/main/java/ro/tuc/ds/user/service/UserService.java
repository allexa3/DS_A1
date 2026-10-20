package ro.tuc.ds.user.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import ro.tuc.ds.user.client.DeviceClient;
import ro.tuc.ds.user.dto.UserCreateRequest;
import ro.tuc.ds.user.dto.UserResponse;
import ro.tuc.ds.user.dto.UserUpdateRequest;
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
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UserService(UserRepository repository, DeviceClient deviceClient) {
        this.repository = repository;
        this.deviceClient = deviceClient;
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
        return UserResponse.from(repository.save(u));
    }

    public void delete(UUID id) {
        getOrThrow(id);
        repository.deleteById(id);
        deviceClient.unassignAllDevicesOf(id);
        LOGGER.debug("User {} deleted", id);
    }

    private User getOrThrow(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));
    }
}
