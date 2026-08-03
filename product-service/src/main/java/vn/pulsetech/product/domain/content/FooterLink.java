package vn.pulsetech.product.domain.content;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "footer_links")
public record FooterLink(
        @Id String id,
        String title,
        List<LinkItem> links,
        int orderIndex
) {
    public record LinkItem(
            String name,
            String link
    ) {}
}
