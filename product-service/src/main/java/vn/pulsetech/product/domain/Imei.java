package vn.pulsetech.product.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "imeis")
public record Imei(
    @Id String id,
    String imeiNumber,
    String productId,
    String productName,
    String storageVariant,
    String status, // IN_STOCK, SOLD, IN_REPAIR, RETURNED
    String orderId, // Can be null if not sold
    LocalDateTime createdAt
) {
}
