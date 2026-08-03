package vn.pulsetech.product.domain.content;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "mega_menus")
public record MegaMenu(
        @Id String id,
        String name,
        String icon,
        String link,
        List<MegaMenuSection> sections,
        int orderIndex
) {
    public record MegaMenuSection(
            String title,
            List<MegaMenuLink> links
    ) {}

    public record MegaMenuLink(
            String name,
            String link
    ) {}
}
