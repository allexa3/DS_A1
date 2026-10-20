package ro.tuc.ds.device.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ro.tuc.ds.device.dto.AssignRequest;
import ro.tuc.ds.device.dto.DeviceRequest;
import ro.tuc.ds.device.dto.DeviceResponse;
import ro.tuc.ds.device.service.DeviceService;

import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceService service;

    public DeviceController(DeviceService service) {
        this.service = service;
    }

    @Operation(summary = "List all devices", description = "Returns 200 with an array of devices.")
    @GetMapping
    public List<DeviceResponse> getAll() {
        return service.findAll();
    }

    @Operation(summary = "Get a device by id", description = "Returns 200 with the device, or 404 if the id does not exist.")
    @GetMapping("/{id}")
    public DeviceResponse getById(@PathVariable UUID id) {
        return service.findById(id);
    }

    @Operation(summary = "List the devices assigned to a user", description = "Returns 200 with an array (empty if none).")
    @GetMapping("/user/{userId}")
    public List<DeviceResponse> getByUser(@PathVariable UUID userId) {
        return service.findByUser(userId);
    }

    @Operation(summary = "Create a device",
            description = "Body: name, description (optional), maxConsumption (> 0). Returns 201 with the device, 400 on invalid input.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DeviceResponse create(@Valid @RequestBody DeviceRequest request) {
        return service.create(request);
    }

    @Operation(summary = "Update a device", description = "Body as for create. Returns 200 with the device, 400/404 on error. The assignment is kept.")
    @PutMapping("/{id}")
    public DeviceResponse update(@PathVariable UUID id, @Valid @RequestBody DeviceRequest request) {
        return service.update(id, request);
    }

    @Operation(summary = "Delete a device", description = "Returns 204 on success, 404 if the id does not exist.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }

    @Operation(summary = "Assign a device to a user",
            description = "Body: userId. The user must exist in the User service. Returns 200 with the device, "
                    + "404 if the device or user is unknown, 503 if the User service is down.")
    @PutMapping("/{id}/user")
    public DeviceResponse assign(@PathVariable UUID id, @Valid @RequestBody AssignRequest request) {
        return service.assign(id, request.userId());
    }

    @Operation(summary = "Remove the user assignment of a device", description = "Returns 200 with the device, or 404.")
    @DeleteMapping("/{id}/user")
    public DeviceResponse unassign(@PathVariable UUID id) {
        return service.unassign(id);
    }

    @Operation(summary = "Unassign every device of a user",
            description = "Called by the User service when a user is deleted. Returns 204.")
    @DeleteMapping("/user/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unassignAllOfUser(@PathVariable UUID userId) {
        service.unassignAllOfUser(userId);
    }
}
