package vn.pulsetech.product.controller;

import org.springframework.web.bind.annotation.*;
import vn.pulsetech.product.domain.InventoryLog;
import vn.pulsetech.product.repository.InventoryLogRepository;

import java.util.List;

@RestController
@RequestMapping("/api/products/inventory-logs")
public class InventoryLogController {
    private final InventoryLogRepository repository;

    public InventoryLogController(InventoryLogRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<InventoryLog> getAllLogs(@RequestParam(required = false) String productId) {
        if (productId != null && !productId.isBlank()) {
            return repository.findByProductIdOrderByCreatedAtDesc(productId);
        }
        return repository.findAllByOrderByCreatedAtDesc();
    }
}
