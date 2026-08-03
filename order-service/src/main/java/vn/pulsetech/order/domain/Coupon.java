package vn.pulsetech.order.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

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
        boolean isActive
) {
    public boolean isValid() {
        return isActive && 
               currentUsage < maxUsage && 
               LocalDateTime.now().isAfter(validFrom) && 
               LocalDateTime.now().isBefore(validUntil);
    }
}
