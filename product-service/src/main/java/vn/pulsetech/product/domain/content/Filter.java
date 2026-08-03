package vn.pulsetech.product.domain.content;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "filters")
public record Filter(
    @Id String id,
    String filterId, // e.g. "os", "ram"
    String name,
    List<String> options,
    List<String> categories
) {}
