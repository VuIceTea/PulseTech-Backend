package vn.pulsetech.product.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "reviews")
public record Review(
        @Id String id,
        String productId,
        String userId,
        String userName,
        int rating,
        String comment,
        LocalDateTime createdAt
) {}
