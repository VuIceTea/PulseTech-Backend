package vn.pulsetech.product.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "products")
public record Product(
        @Id String id,
        String name,
        String brand,
        String category,
        long basePrice,
        long originalPrice,
        int discount,
        String image,
        List<String> images,
        List<ColorVariant> colors,
        List<StorageVariant> storages,
        ProductSpec specs,
        String description,
        double rating,
        int reviewsCount,
        boolean isFeatured,
        boolean isFlashSale,
        String badge,
        int stock
) {
    public Product withDiscountAndPrice(int newDiscount, long newBasePrice) {
        return new Product(id, name, brand, category, newBasePrice, originalPrice, newDiscount, image, images, colors, storages, specs, description, rating, reviewsCount, isFeatured, isFlashSale, badge, stock);
    }

    public record ColorVariant(
            String name,
            String hex,
            String image
    ) {}

    public record StorageVariant(
            String name,
            long priceOffset
    ) {}

    public record ProductSpec(
            String screen,
            String os,
            String camera,
            String frontCamera,
            String cpu,
            String ram,
            String storage,
            String battery,
            String accessoryType,
            String headphoneType,
            String audioFeature,
            String connectionType,
            String cableLength,
            String chargingPower,
            String chargingPorts,
            String caseMaterial,
            String caseFeature
    ) {}
}