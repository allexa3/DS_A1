package ro.tuc.ds.device.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import ro.tuc.ds.device.exception.UpstreamUnavailableException;

import java.util.UUID;

/** Talks to the User service over the internal Docker network (not through the reverse proxy). */
@Component
public class UserClient {

    private final RestClient client;

    public UserClient(@Value("${user.service.url}") String baseUrl) {
        this.client = RestClient.create(baseUrl);
    }

    public boolean exists(UUID userId) {
        try {
            client.get().uri("/api/users/{id}", userId).retrieve().toBodilessEntity();
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        } catch (RestClientException e) {
            throw new UpstreamUnavailableException("User service is not reachable: " + e.getMessage());
        }
    }
}
