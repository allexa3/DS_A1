package ro.tuc.ds.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.tuc.ds.auth.entity.Credential;

import java.util.Optional;
import java.util.UUID;

public interface CredentialRepository extends JpaRepository<Credential, UUID> {
    Optional<Credential> findByUsername(String username);
}
