package ro.tuc.ds.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.tuc.ds.user.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByUsername(String username);

    Optional<User> findByUsername(String username);
}
