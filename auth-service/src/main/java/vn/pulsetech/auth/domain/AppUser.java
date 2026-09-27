package vn.pulsetech.auth.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

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
    private Set<String> roles;
    private String phone;
    private String dob;
    private String gender;

    protected AppUser() {}

    public AppUser(String name, String email, String passwordHash) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.verified = false;
        this.rewardPoints = 0;
        this.createdAt = Instant.now();
        this.locked = false;
        this.roles = new LinkedHashSet<>(Set.of("USER"));
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
    public Set<String> getRoles() {
        if (roles == null || roles.isEmpty()) roles = new LinkedHashSet<>(Set.of("USER"));
        return roles;
    }
    public void setRoles(Set<String> roles) { this.roles = new LinkedHashSet<>(roles); }
    public void addRewardPoints(int points) { this.rewardPoints += points; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getTier() {
        if (rewardPoints >= 15000) return "KIM_CUONG";
        if (rewardPoints >= 5000) return "VANG";
        if (rewardPoints >= 1000) return "BAC";
        return "DONG";
    }
}
