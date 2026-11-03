package ro.tuc.ds.auth.controller;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ro.tuc.ds.auth.dto.CredentialSyncRequest;
import ro.tuc.ds.auth.service.AuthService;

import java.util.UUID;

/**
 * Service-to-service API used by the User service. The reverse proxy has no route for /internal, so it is only
 * reachable from inside the Docker network.
 */
@Hidden
@RestController
@RequestMapping("/internal/credentials")
public class InternalCredentialController {

    private final AuthService service;

    public InternalCredentialController(AuthService service) {
        this.service = service;
    }

    @PutMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void upsert(@PathVariable UUID userId, @Valid @RequestBody CredentialSyncRequest request) {
        service.upsertCredential(userId, request);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID userId) {
        service.deleteCredential(userId);
    }
}
