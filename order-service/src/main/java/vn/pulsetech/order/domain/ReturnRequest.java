package vn.pulsetech.order.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "return_requests")
public record ReturnRequest(
        @Id String id,
        String orderId,
        String customerEmail,
        String customerName,
        String customerPhone,
        String reason,
        String description,
        String imageUrl,
        String status, // PENDING, APPROVED, REJECTED, RESTOCKED
        long refundAmount,
        LocalDateTime createdAt
) {
    public ReturnRequest withStatus(String newStatus) {
        return new ReturnRequest(id, orderId, customerEmail, customerName, customerPhone, reason, description, imageUrl, newStatus, refundAmount, createdAt);
    }
}
