package ro.tuc.ds.device.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "devices")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String description;

    /** Maximum consumption in kWh. */
    @Column(name = "max_consumption", nullable = false)
    private double maxConsumption;

    /**
     * Owner of the device. It is a plain id, not a foreign key: users live in another
     * microservice with its own database, so the link is kept at application level.
     */
    @Column(name = "user_id")
    private UUID userId;

    public Device() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getMaxConsumption() { return maxConsumption; }
    public void setMaxConsumption(double maxConsumption) { this.maxConsumption = maxConsumption; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
}
