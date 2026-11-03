package ro.tuc.ds.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import ro.tuc.ds.auth.client.UserClient;
import ro.tuc.ds.auth.dto.CredentialSyncRequest;
import ro.tuc.ds.auth.dto.LoginRequest;
import ro.tuc.ds.auth.dto.LoginResponse;
import ro.tuc.ds.auth.dto.RegisterRequest;
import ro.tuc.ds.auth.dto.RegisteredUserResponse;
import ro.tuc.ds.auth.entity.Credential;
import ro.tuc.ds.auth.exception.DuplicateResourceException;
import ro.tuc.ds.auth.exception.InvalidCredentialsException;
import ro.tuc.ds.auth.repository.CredentialRepository;
import ro.tuc.ds.auth.security.AuthenticatedUser;
import ro.tuc.ds.auth.security.JwtService;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);

    private final CredentialRepository repository;
    private final JwtService jwt;
    private final UserClient userClient;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthService(CredentialRepository repository, JwtService jwt, UserClient userClient) {
        this.repository = repository;
        this.jwt = jwt;
        this.userClient = userClient;
    }

    public LoginResponse login(LoginRequest req) {
        // Same error for "unknown user" and "wrong password" so usernames cannot be probed.
        Credential c = repository.findByUsername(req.username())
                .filter(found -> encoder.matches(req.password(), found.getPasswordHash()))
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));
        LOGGER.debug("User {} logged in", c.getUserId());
        return new LoginResponse(jwt.generate(c), "Bearer", jwt.lifetimeSeconds(),
                c.getUserId(), c.getUsername(), c.getRole());
    }

    public RegisteredUserResponse register(RegisterRequest req) {
        return userClient.createClient(req);
    }

    /**
     * Resolves a bearer token to the identity it represents. The role is taken from the credential database, not
     * from the token, so a deleted user or a changed role takes effect immediately.
     */
    public Optional<AuthenticatedUser> authenticate(String token) {
        return jwt.parse(token)
                .flatMap(claims -> repository.findById(claims.userId()))
                .map(c -> new AuthenticatedUser(c.getUserId(), c.getUsername(), c.getRole()));
    }

    /** Called by the User service: creates or updates the credential of a user (idempotent). */
    public void upsertCredential(UUID userId, CredentialSyncRequest req) {
        Optional<Credential> sameName = repository.findByUsername(req.username());
        if (sameName.isPresent() && !sameName.get().getUserId().equals(userId)) {
            throw new DuplicateResourceException("Username already taken: " + req.username());
        }
        Credential c = repository.findById(userId).orElseGet(() -> {
            Credential fresh = new Credential();
            fresh.setUserId(userId);
            return fresh;
        });
        c.setUsername(req.username());
        c.setRole(req.role());
        boolean hasPassword = req.password() != null && !req.password().isBlank();
        if (hasPassword) {
            c.setPasswordHash(encoder.encode(req.password()));
        } else if (c.getPasswordHash() == null) {
            throw new IllegalArgumentException("A password is required to create a credential");
        }
        repository.save(c);
    }

    public void deleteCredential(UUID userId) {
        if (repository.existsById(userId)) {
            repository.deleteById(userId);
        }
    }
}
