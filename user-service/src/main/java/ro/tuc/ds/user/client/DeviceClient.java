package ro.tuc.ds.user.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

/** Talks to the Device service over the internal Docker network (not through the reverse proxy). */
@Component
public class DeviceClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceClient.class);
    private final RestClient client;

    public DeviceClient(@Value("${device.service.url}") String baseUrl) {
        this.client = RestClient.create(baseUrl);
    }

    /** Best effort: a deleted user must not keep devices assigned to them. */
    public void unassignAllDevicesOf(UUID userId) {
        try {
            client.delete().uri("/api/devices/user/{userId}", userId).retrieve().toBodilessEntity();
        } catch (RestClientException e) {
            LOGGER.warn("Could not unassign devices of deleted user {}: {}", userId, e.getMessage());
        }
    }
}
