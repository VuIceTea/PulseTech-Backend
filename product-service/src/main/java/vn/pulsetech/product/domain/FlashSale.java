package vn.pulsetech.product.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "flash_sales")
public record FlashSale(
        @Id String id,
        String title,
        LocalDateTime startTime,
        LocalDateTime endTime,
        boolean isActive,
        List<FlashSaleItem> items
) {
    public record FlashSaleItem(
            String productId,
            String productName,
            String variantName,
            long originalPrice,
            long flashPrice,
            int limitQuantity,
            int soldQuantity,
            String image
    ) {}
}
