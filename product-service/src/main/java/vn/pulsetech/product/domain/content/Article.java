package vn.pulsetech.product.domain.content;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "articles")
public record Article(
        @Id String id,
        String title,
        String slug,
        String summary,
        String content,
        String imageUrl,
        String author,
        LocalDateTime publishedAt,
        String category,
        int viewCount
) {}
