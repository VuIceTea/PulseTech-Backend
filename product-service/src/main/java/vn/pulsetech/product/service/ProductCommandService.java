package vn.pulsetech.product.service;

import org.springframework.stereotype.Service;
import vn.pulsetech.product.domain.Product;
import vn.pulsetech.product.repository.ProductCatalogRepository;

@Service
public class ProductCommandService {
    private final ProductCatalogRepository repository;

    public ProductCommandService(ProductCatalogRepository repository) {
        this.repository = repository;
    }

    public Product updateDiscount(String id, int discount) {
        Product product = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Product not found"));
        long newBasePrice = Math.round(product.originalPrice() * (100.0 - discount) / 100.0);
        Product updatedProduct = product.withDiscountAndPrice(discount, newBasePrice);
        return repository.save(updatedProduct);
    }

    public Product save(Product product) {
        if (product.storages() != null && !product.storages().isEmpty()) {
            int totalStock = 0;
            boolean allVariantsHaveStock = true;
            for (var storage : product.storages()) {
                if (storage.stock() != null) {
                    totalStock += storage.stock();
                } else {
                    allVariantsHaveStock = false;
                }
            }
            if (allVariantsHaveStock) {
                product = product.withStock(totalStock);
            }
        }
        return repository.save(product);
    }

    public void deleteById(String id) {
        repository.deleteById(id);
    }
}
