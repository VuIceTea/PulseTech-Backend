package vn.pulsetech.product.domain.content;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "navigation")
public record Navigation(
    @Id String id,
    String title,
    String href,
    String icon,
    int order
) {}
