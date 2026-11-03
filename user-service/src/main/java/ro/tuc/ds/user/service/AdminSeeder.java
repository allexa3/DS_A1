package ro.tuc.ds.user.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Creates the first administrator, otherwise nobody could log in to create other users.
 * Runs in the background and retries, because the Authentication service may still be starting.
 */
@Component
public class AdminSeeder {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminSeeder.class);
    private static final int MAX_ATTEMPTS = 40;
    private static final long WAIT_MS = 3000;

    private final UserService userService;
    private final String username;
    private final String password;

    public AdminSeeder(UserService userService,
                       @Value("${app.admin.username}") String username,
                       @Value("${app.admin.password}") String password) {
        this.userService = userService;
        this.username = username;
        this.password = password;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        if (password == null || password.length() < 6) {
            LOGGER.warn("ADMIN_PASSWORD is missing or shorter than 6 characters: no administrator is seeded");
            return;
        }
        Thread t = new Thread(this::seed, "admin-seeder");
        t.setDaemon(true);
        t.start();
    }

    private void seed() {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                userService.ensureAdmin(username, password);
                LOGGER.info("Administrator '{}' is ready", username);
                return;
            } catch (RuntimeException e) {
                LOGGER.info("Administrator seeding attempt {}/{} failed: {}", attempt, MAX_ATTEMPTS, e.getMessage());
                try {
                    Thread.sleep(WAIT_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
        LOGGER.error("Could not seed the administrator after {} attempts", MAX_ATTEMPTS);
    }
}
