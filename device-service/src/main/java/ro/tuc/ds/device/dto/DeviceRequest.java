package ro.tuc.ds.device.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DeviceRequest(
        @NotBlank String name,
        String description,
        @NotNull @Positive Double maxConsumption) {
}
