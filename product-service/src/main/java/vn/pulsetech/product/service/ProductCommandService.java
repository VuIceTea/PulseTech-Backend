package vn.pulsetech.product.service;

import org.springframework.stereotype.Service;
import vn.pulsetech.product.domain.Product;
import vn.pulsetech.product.repository.ProductCatalogRepository;

import java.util.ArrayList;
import java.util.List;

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
            for (var storage : product.storages()) {
                if (storage.stock() != null) {
                    totalStock += storage.stock();
                }
            }
            product = product.withStock(totalStock);
        } else {
            product = product.withStock(0);
        }
        return repository.save(product);
    }

    public Product decrementStorageStock(String productId, String storageName, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        Product product = repository.findById(productId).orElseThrow(() -> new IllegalArgumentException("Product not found"));
        if (product.storages() == null || product.storages().isEmpty()) {
            throw new IllegalArgumentException("Product has no storage variants");
        }

        boolean matched = false;
        int totalStock = 0;
        List<Product.StorageVariant> updatedStorages = new ArrayList<>();
        for (Product.StorageVariant storage : product.storages()) {
            Integer stock = storage.stock();
            int safeStock = stock == null ? 0 : stock;
            if (storage.name().equals(storageName)) {
                matched = true;
                if (safeStock < quantity) {
                    throw new IllegalArgumentException("Not enough stock for variant");
                }
                safeStock -= quantity;
                updatedStorages.add(new Product.StorageVariant(storage.name(), storage.priceOffset(), safeStock, storage.specs()));
            } else {
                updatedStorages.add(storage);
            }
            totalStock += safeStock;
        }

        if (!matched) {
            throw new IllegalArgumentException("Storage variant not found");
        }

        return repository.save(product.withStoragesAndStock(updatedStorages, totalStock));
    }

    public void deleteById(String id) {
        repository.deleteById(id);
    }
}
