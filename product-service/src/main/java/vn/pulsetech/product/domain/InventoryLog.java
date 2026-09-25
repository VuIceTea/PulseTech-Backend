package vn.pulsetech.product.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "inventory_logs")
public record InventoryLog(
        @Id String id,
        String productId,
        String productName,
        String storageVariant,
        int previousStock,
        int newStock,
        int changeQuantity,
        String reason, // IMPORT, EXPORT, RETURN_RESTOCK, MANUAL_ADJUSTMENT, UNPAID_CANCEL
        String performedBy,
        LocalDateTime createdAt
) {}
