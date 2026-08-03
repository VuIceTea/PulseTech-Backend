package vn.pulsetech.product.domain.content;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "policies")
public record Policy(
        @Id String id,
        String title,
        String icon,
        String contentHtml,
        int orderIndex
) {}
