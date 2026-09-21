package vn.pulsetech.auth.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document(collection = "users")
public class AppUser {
    @Id
    private String id;
    private String name;
    @Indexed(unique = true)
    private String email;
    private String passwordHash;
    private boolean verified;
    private int rewardPoints;
    private Instant createdAt;
    private boolean locked;

    protected AppUser() {}

    public AppUser(String name, String email, String passwordHash) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.verified = false;
        this.rewardPoints = 0;
        this.createdAt = Instant.now();
        this.locked = false;
    }

    public void markVerified() { this.verified = true; }
    public void setLocked(boolean locked) { this.locked = locked; }
    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public boolean isVerified() { return verified; }
    public int getRewardPoints() { return rewardPoints; }
    public Instant getCreatedAt() { return createdAt; }
    public boolean isLocked() { return locked; }
    public void addRewardPoints(int points) { this.rewardPoints += points; }
}
