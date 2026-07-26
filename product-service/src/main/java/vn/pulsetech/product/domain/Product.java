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
        List<Review> reviews,
        boolean isFeatured,
        boolean isFlashSale,
        String badge,
        int stock
) {

    public record ColorVariant(
            String name,
            String hex,
            String image
    ) {}

    public record StorageVariant(
            String name,
            long priceOffset
    ) {}

    public record Review(
            String id,
            String user,
            int rating,
            String comment,
            String date
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