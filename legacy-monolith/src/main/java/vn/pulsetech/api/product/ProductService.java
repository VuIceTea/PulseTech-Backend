package vn.pulsetech.api.product;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class ProductService {
    private final List<JsonNode> products;

    public ProductService(ObjectMapper objectMapper) throws IOException {
        try (var input = new ClassPathResource("products.json").getInputStream()) {
            products = List.copyOf(objectMapper.readValue(input, new TypeReference<>() {}));
        }
    }

    public List<JsonNode> findAll(String category, String brand, String search, Boolean featured, Boolean flashSale) {
        Stream<JsonNode> stream = products.stream();
        if (hasText(category)) stream = stream.filter(p -> category.equalsIgnoreCase(p.path("category").asText()));
        if (hasText(brand)) stream = stream.filter(p -> brand.equalsIgnoreCase(p.path("brand").asText()));
        if (hasText(search)) {
            String query = search.toLowerCase(Locale.ROOT);
            stream = stream.filter(p -> p.path("name").asText().toLowerCase(Locale.ROOT).contains(query)
                    || p.path("brand").asText().toLowerCase(Locale.ROOT).contains(query));
        }
        if (featured != null) stream = stream.filter(p -> p.path("isFeatured").asBoolean(false) == featured);
        if (flashSale != null) stream = stream.filter(p -> p.path("isFlashSale").asBoolean(false) == flashSale);
        return stream.toList();
    }

    public Optional<JsonNode> findById(String id) {
        return products.stream().filter(p -> id.equals(p.path("id").asText())).findFirst();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

