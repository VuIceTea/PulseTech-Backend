package vn.pulsetech.product.config;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import vn.pulsetech.product.domain.Product;
import vn.pulsetech.product.repository.ProductCatalogRepository;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

@Configuration
public class DemoDataConfig {
    @Bean
    ApplicationRunner seedProductData(ProductCatalogRepository repository, ObjectMapper objectMapper) {
        return args -> {
            try (InputStream input = new ClassPathResource("products.json").getInputStream()) {
                List<Product> products = objectMapper.readValue(input, new TypeReference<List<Product>>() {});
                for (Product seedProduct : products) {
                    Product seedWithContent = ensureContent(seedProduct);
                    Optional<Product> existingProduct = repository.findById(seedProduct.id());

                    if (existingProduct.isEmpty()) {
                        repository.save(seedWithContent);
                    } else if (isBlank(existingProduct.get().content())) {
                        // Backfill only legacy records that have no article; never overwrite admin edits.
                        repository.save(existingProduct.get().withContent(seedWithContent.content()));
                    }
                }
            }
        };
    }

    private Product ensureContent(Product product) {
        return isBlank(product.content())
                ? product.withContent(ProductContentDefaults.forProduct(product))
                : product;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
