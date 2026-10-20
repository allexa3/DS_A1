package ro.tuc.ds.device.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.tuc.ds.device.client.UserClient;
import ro.tuc.ds.device.dto.DeviceRequest;
import ro.tuc.ds.device.dto.DeviceResponse;
import ro.tuc.ds.device.entity.Device;
import ro.tuc.ds.device.exception.ResourceNotFoundException;
import ro.tuc.ds.device.repository.DeviceRepository;

import java.util.List;
import java.util.UUID;

@Service
public class DeviceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceService.class);

    private final DeviceRepository repository;
    private final UserClient userClient;

    public DeviceService(DeviceRepository repository, UserClient userClient) {
        this.repository = repository;
        this.userClient = userClient;
    }

    public List<DeviceResponse> findAll() {
        return repository.findAll().stream().map(DeviceResponse::from).toList();
    }

    public DeviceResponse findById(UUID id) {
        return DeviceResponse.from(getOrThrow(id));
    }

    public List<DeviceResponse> findByUser(UUID userId) {
        return repository.findByUserId(userId).stream().map(DeviceResponse::from).toList();
    }

    public DeviceResponse create(DeviceRequest req) {
        Device d = new Device();
        apply(d, req);
        d = repository.save(d);
        LOGGER.debug("Device {} created", d.getId());
        return DeviceResponse.from(d);
    }

    public DeviceResponse update(UUID id, DeviceRequest req) {
        Device d = getOrThrow(id);
        apply(d, req);
        return DeviceResponse.from(repository.save(d));
    }

    public void delete(UUID id) {
        getOrThrow(id);
        repository.deleteById(id);
    }

    public DeviceResponse assign(UUID deviceId, UUID userId) {
        Device d = getOrThrow(deviceId);
        if (!userClient.exists(userId)) {
            throw new ResourceNotFoundException("User with id " + userId + " not found");
        }
        d.setUserId(userId);
        return DeviceResponse.from(repository.save(d));
    }

    public DeviceResponse unassign(UUID deviceId) {
        Device d = getOrThrow(deviceId);
        d.setUserId(null);
        return DeviceResponse.from(repository.save(d));
    }

    @Transactional
    public void unassignAllOfUser(UUID userId) {
        int n = repository.unassignUser(userId);
        LOGGER.debug("Unassigned {} devices from user {}", n, userId);
    }

    private void apply(Device d, DeviceRequest req) {
        d.setName(req.name());
        d.setDescription(req.description());
        d.setMaxConsumption(req.maxConsumption());
    }

    private Device getOrThrow(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device with id " + id + " not found"));
    }
}
