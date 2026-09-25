package vn.pulsetech.product.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "warranties")
public record Warranty(
        @Id String id,
        String imei,
        String orderId,
        String productId,
        String productName,
        String storageVariant,
        String customerName,
        String customerPhone,
        String customerEmail,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String status, // ACTIVE, EXPIRED, IN_REPAIR, VOID
        String notes
) {
    public Warranty withStatusAndNotes(String newStatus, String newNotes) {
        return new Warranty(id, imei, orderId, productId, productName, storageVariant, customerName, customerPhone, customerEmail, startDate, endDate, newStatus, newNotes);
    }
}
