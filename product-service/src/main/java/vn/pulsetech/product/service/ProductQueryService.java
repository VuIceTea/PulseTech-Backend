package vn.pulsetech.product.service;

import org.springframework.stereotype.Service;
import vn.pulsetech.product.domain.Product;
import vn.pulsetech.product.repository.ProductCatalogRepository;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class ProductQueryService {
    private final ProductCatalogRepository repository;

    public ProductQueryService(ProductCatalogRepository repository) { this.repository = repository; }

    public List<Product> findAll(String category, String brand, String search, Boolean featured, Boolean flashSale) {
        Stream<Product> stream = repository.findAll().stream();
        if (hasText(category)) stream = stream.filter(p -> category.equalsIgnoreCase(p.category()));
        if (hasText(brand)) stream = stream.filter(p -> brand.equalsIgnoreCase(p.brand()));
        if (hasText(search)) {
            String query = search.toLowerCase(Locale.ROOT);
            stream = stream.filter(p -> p.name().toLowerCase(Locale.ROOT).contains(query)
                    || p.brand().toLowerCase(Locale.ROOT).contains(query));
        }
        if (featured != null) stream = stream.filter(p -> p.isFeatured() == featured);
        if (flashSale != null) stream = stream.filter(p -> p.isFlashSale() == flashSale);
        return stream.toList();
    }

    public Optional<Product> findById(String id) { return repository.findById(id); }
    private boolean hasText(String value) { return value != null && !value.isBlank(); }
}
