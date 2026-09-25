package vn.pulsetech.product.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import vn.pulsetech.product.domain.InventoryLog;

import java.util.List;

public interface InventoryLogRepository extends MongoRepository<InventoryLog, String> {
    List<InventoryLog> findByProductIdOrderByCreatedAtDesc(String productId);
    List<InventoryLog> findAllByOrderByCreatedAtDesc();
}
