package ro.tuc.ds.user.controller;

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
import ro.tuc.ds.user.dto.UserCreateRequest;
import ro.tuc.ds.user.dto.UserResponse;
import ro.tuc.ds.user.dto.UserUpdateRequest;
import ro.tuc.ds.user.service.UserService;

import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping("/api/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @Operation(summary = "List all users", description = "Returns 200 with an array of users (passwords are never returned).")
    @GetMapping
    public List<UserResponse> getAll() {
        return service.findAll();
    }

    @Operation(summary = "Get a user by id", description = "Returns 200 with the user, or 404 if the id does not exist.")
    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable UUID id) {
        return service.findById(id);
    }

    @Operation(summary = "Create a user",
            description = "Body: username, password (min 6), name, email, role (ADMIN|CLIENT). "
                    + "Returns 201 with the created user, 400 on invalid input, 409 if the username is taken.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody UserCreateRequest request) {
        return service.create(request);
    }

    @Operation(summary = "Update a user",
            description = "Body as for create; password is optional (kept if omitted). "
                    + "Returns 200 with the updated user, 400/404/409 on error.")
    @PutMapping("/{id}")
    public UserResponse update(@PathVariable UUID id, @Valid @RequestBody UserUpdateRequest request) {
        return service.update(id, request);
    }

    @Operation(summary = "Delete a user",
            description = "Returns 204 on success, 404 if the id does not exist. Devices assigned to the user are unassigned.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
