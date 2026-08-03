package vn.pulsetech.product.domain.content;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "categories")
public record Category(
    @Id String id,
    String name,
    String icon,
    int count,
    int order
) {}
