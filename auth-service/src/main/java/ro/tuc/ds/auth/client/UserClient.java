package ro.tuc.ds.auth.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import ro.tuc.ds.auth.dto.RegisterRequest;
import ro.tuc.ds.auth.dto.RegisteredUserResponse;
import ro.tuc.ds.auth.exception.DuplicateResourceException;
import ro.tuc.ds.auth.exception.UpstreamUnavailableException;

/** Talks to the User service over the internal Docker network (not through the reverse proxy). */
@Component
public class UserClient {

    private record CreateUserBody(String username, String password, String name, String email, String role) {
    }

    private final RestClient client;

    public UserClient(@Value("${user.service.url}") String baseUrl) {
        this.client = RestClient.create(baseUrl);
    }

    /** Creates a CLIENT user. The User service in turn registers the credential in this service. */
    public RegisteredUserResponse createClient(RegisterRequest req) {
        try {
            return client.post().uri("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new CreateUserBody(req.username(), req.password(), req.name(), req.email(), "CLIENT"))
                    .retrieve()
                    .body(RegisteredUserResponse.class);
        } catch (HttpClientErrorException.Conflict e) {
            throw new DuplicateResourceException("Username already taken: " + req.username());
        } catch (RestClientException e) {
            throw new UpstreamUnavailableException("User service is not reachable: " + e.getMessage());
        }
    }
}
