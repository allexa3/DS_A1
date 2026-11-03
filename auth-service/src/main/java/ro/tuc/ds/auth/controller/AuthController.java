package ro.tuc.ds.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ro.tuc.ds.auth.dto.LoginRequest;
import ro.tuc.ds.auth.dto.LoginResponse;
import ro.tuc.ds.auth.dto.RegisterRequest;
import ro.tuc.ds.auth.dto.RegisteredUserResponse;
import ro.tuc.ds.auth.security.AccessPolicy;
import ro.tuc.ds.auth.security.AuthenticatedUser;
import ro.tuc.ds.auth.service.AuthService;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String BEARER = "Bearer ";

    private final AuthService service;
    private final AccessPolicy policy;

    public AuthController(AuthService service, AccessPolicy policy) {
        this.service = service;
        this.policy = policy;
    }

    @Operation(summary = "Log in",
            description = "Body: username, password. Returns 200 with a JWT (use it as 'Authorization: Bearer <token>'), "
                    + "the user id and the role; 400 on invalid input, 401 on wrong credentials.")
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return service.login(request);
    }

    @Operation(summary = "Register a new client account",
            description = "Body: username, password (min 6), name, email. The account gets the CLIENT role. "
                    + "Returns 201 with the created user, 400 on invalid input, 409 if the username is taken, "
                    + "503 if the User service is down.")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisteredUserResponse register(@Valid @RequestBody RegisterRequest request) {
        return service.register(request);
    }

    /**
     * Called by the reverse proxy (Traefik forwardAuth) before every protected request; never exposed publicly.
     * Traefik passes the original method and URI in X-Forwarded-Method / X-Forwarded-Uri.
     */
    @Operation(summary = "Validate a token and authorize a request (used by the gateway)",
            description = "Reads the Authorization header plus X-Forwarded-Method and X-Forwarded-Uri. "
                    + "Returns 200 with X-User-Id, X-User-Name and X-User-Role headers when the request is allowed, "
                    + "401 when the token is missing, invalid or expired, 403 when the role may not do this.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @RequestMapping("/validate")
    public ResponseEntity<Void> validate(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestHeader(value = "X-Forwarded-Method", defaultValue = "GET") String method,
            @RequestHeader(value = "X-Forwarded-Uri", defaultValue = "") String uri) {
        if (authorization == null || !authorization.startsWith(BEARER)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Optional<AuthenticatedUser> user = service.authenticate(authorization.substring(BEARER.length()).trim());
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!policy.isAllowed(user.get(), method, uri)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok()
                .header("X-User-Id", user.get().userId().toString())
                .header("X-User-Name", user.get().username())
                .header("X-User-Role", user.get().role().name())
                .build();
    }
}
