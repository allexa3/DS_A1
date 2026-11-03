package ro.tuc.ds.user.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import ro.tuc.ds.user.entity.Role;
import ro.tuc.ds.user.exception.UpstreamUnavailableException;

import java.util.UUID;

/**
 * Keeps the Credential database of the Authentication service in sync with the users managed here.
 * Uses the internal Docker network (/internal is not routed by the reverse proxy).
 */
@Component
public class AuthClient {

    private record CredentialBody(String username, String password, Role role) {
    }

    private final RestClient client;

    public AuthClient(@Value("${auth.service.url}") String baseUrl) {
        this.client = RestClient.create(baseUrl);
    }

    /** Creates or updates the credential. A null password keeps the current one. */
    public void syncCredential(UUID userId, String username, String password, Role role) {
        try {
            client.put().uri("/internal/credentials/{id}", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new CredentialBody(username, password, role))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw new UpstreamUnavailableException("Authentication service could not store the credential: " + e.getMessage());
        }
    }

    public void deleteCredential(UUID userId) {
        try {
            client.delete().uri("/internal/credentials/{id}", userId).retrieve().toBodilessEntity();
        } catch (RestClientException e) {
            throw new UpstreamUnavailableException("Authentication service could not remove the credential: " + e.getMessage());
        }
    }
}
