package vn.pulsetech.order.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Document(collection = "coupons")
public record Coupon(
        @Id String id,
        String code,
        String description,
        double discountPercent,
        double discountAmount,
        long minOrderValue,
        long maxDiscountValue,
        LocalDateTime validFrom,
        LocalDateTime validUntil,
        int currentUsage,
        int maxUsage,
        boolean isActive,
        List<String> assignedEmails,
        List<String> usedEmails
) {
    public boolean isValid() {
        return isActive && 
               currentUsage < maxUsage && 
               LocalDateTime.now().isAfter(validFrom) && 
               LocalDateTime.now().isBefore(validUntil);
    }
    
    public boolean isAllowedFor(String email) {
        if (email == null || email.isBlank()) return false;
        return remainingUsesFor(email) > 0;
    }

    public boolean isVisibleFor(String email) {
        if (assignedEmails == null || assignedEmails.isEmpty() || email == null || email.isBlank()) return false;
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        return assignedEmails.stream()
                .anyMatch(value -> value != null && normalizedEmail.equals(value.trim().toLowerCase(Locale.ROOT)));
    }

    public int remainingUsesFor(String email) {
        if (assignedEmails == null || assignedEmails.isEmpty() || email == null || email.isBlank()) return 0;
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        int assignedCount = (int) assignedEmails.stream()
                .filter(value -> value != null && normalizedEmail.equals(value.trim().toLowerCase(Locale.ROOT)))
                .count();
        int usedCount = usedCountFor(email);
        return Math.max(0, assignedCount - usedCount);
    }

    public int usedCountFor(String email) {
        if (usedEmails == null || usedEmails.isEmpty() || email == null || email.isBlank()) return 0;
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        return (int) usedEmails.stream()
                .filter(value -> value != null && normalizedEmail.equals(value.trim().toLowerCase(Locale.ROOT)))
                .count();
    }
}
