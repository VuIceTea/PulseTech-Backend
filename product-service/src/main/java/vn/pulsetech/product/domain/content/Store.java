package vn.pulsetech.product.domain.content;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "stores")
public record Store(
        @Id String id,
        String name,
        String address,
        String phone,
        String mapUrl,
        String openingHours,
        int orderIndex
) {}
