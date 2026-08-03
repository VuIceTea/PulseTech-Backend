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

@Configuration
public class DemoDataConfig {
    @Bean
    ApplicationRunner seedProductData(ProductCatalogRepository repository, ObjectMapper objectMapper) {
        return args -> {
            if (repository.count() == 0) {
                try (InputStream input = new ClassPathResource("products.json").getInputStream()) {
                    List<Product> products = objectMapper.readValue(input, new TypeReference<List<Product>>() {});
                    repository.saveAll(products);
                }
            }
        };
    }
}
