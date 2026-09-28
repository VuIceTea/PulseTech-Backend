package vn.pulsetech.product.service;

import org.springframework.stereotype.Service;
import vn.pulsetech.product.domain.InventoryLog;
import vn.pulsetech.product.domain.Product;
import vn.pulsetech.product.repository.InventoryLogRepository;
import vn.pulsetech.product.repository.ProductCatalogRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductCommandService {
    private final ProductCatalogRepository repository;
    private final InventoryLogRepository inventoryLogRepository;

    public ProductCommandService(ProductCatalogRepository repository, InventoryLogRepository inventoryLogRepository) {
        this.repository = repository;
        this.inventoryLogRepository = inventoryLogRepository;
    }

    public Product updateDiscount(String id, int discount) {
        Product product = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Product not found"));
        long newBasePrice = Math.round(product.originalPrice() * (100.0 - discount) / 100.0);
        Product updatedProduct = product.withDiscountAndPrice(discount, newBasePrice);
        return repository.save(updatedProduct);
    }

    public Product save(Product product) {
        Product existingProduct = null;
        if (product.id() != null) {
            existingProduct = repository.findById(product.id()).orElse(null);
        }

        if (product.storages() != null && !product.storages().isEmpty()) {
            int totalStock = 0;
            for (var storage : product.storages()) {
                int newStock = storage.stock() != null ? storage.stock() : 0;
                totalStock += newStock;

                // Log inventory change if stock differs from existing
                int oldStock = 0;
                if (existingProduct != null && existingProduct.storages() != null) {
                    oldStock = existingProduct.storages().stream()
                            .filter(s -> s.name().equals(storage.name()))
                            .map(s -> s.stock() != null ? s.stock() : 0)
                            .findFirst()
                            .orElse(0);
                }

                if (newStock != oldStock) {
                    int diff = newStock - oldStock;
                    String reason = diff > 0 ? "IMPORT" : "EXPORT"; // Or MANUAL_ADJUST, but IMPORT/EXPORT is supported by UI
                    inventoryLogRepository.save(new InventoryLog(null, product.id() != null ? product.id() : "PENDING_ID", product.name(), storage.name(), oldStock, newStock, diff, reason, "Admin", LocalDateTime.now()));
                }
            }
            product = product.withStock(totalStock);
        } else {
            product = product.withStock(0);
        }
        
        Product savedProduct = repository.save(product);
        
        // Fix up PENDING_ID for new products
        if (product.id() == null) {
            List<InventoryLog> pendingLogs = inventoryLogRepository.findByProductIdOrderByCreatedAtDesc("PENDING_ID");
            for (InventoryLog log : pendingLogs) {
                inventoryLogRepository.save(new InventoryLog(log.id(), savedProduct.id(), log.productName(), log.storageVariant(), log.previousStock(), log.newStock(), log.changeQuantity(), log.reason(), log.performedBy(), log.createdAt()));
            }
        }
        
        return savedProduct;
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
                int newStock = safeStock - quantity;
                inventoryLogRepository.save(new InventoryLog(null, product.id(), product.name(), storage.name(), safeStock, newStock, -quantity, "EXPORT", "System", LocalDateTime.now()));
                safeStock = newStock;
                updatedStorages.add(new Product.StorageVariant(storage.name(), storage.priceOffset(), storage.costPrice(), safeStock, storage.specs()));
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

    public Product incrementStorageStock(String productId, String storageName, int quantity, String reason) {
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
                int newStock = safeStock + quantity;
                inventoryLogRepository.save(new InventoryLog(null, product.id(), product.name(), storage.name(), safeStock, newStock, quantity, reason != null ? reason : "RETURN_RESTOCK", "System", LocalDateTime.now()));
                safeStock = newStock;
                updatedStorages.add(new Product.StorageVariant(storage.name(), storage.priceOffset(), storage.costPrice(), safeStock, storage.specs()));
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
