package ro.tuc.ds.device.dto;

import ro.tuc.ds.device.entity.Device;

import java.util.UUID;

public record DeviceResponse(UUID id, String name, String description, double maxConsumption, UUID userId) {

    public static DeviceResponse from(Device d) {
        return new DeviceResponse(d.getId(), d.getName(), d.getDescription(), d.getMaxConsumption(), d.getUserId());
    }
}
