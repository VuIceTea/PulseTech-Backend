package vn.pulsetech.product.domain.content;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "banners")
public record Banner(
    @Id String id,
    String imageUrl,
    String title,
    String subtitle,
    String promoText,
    String bgColor,
    String link,
    String position, // e.g. "main", "sub"
    int order
) {}
