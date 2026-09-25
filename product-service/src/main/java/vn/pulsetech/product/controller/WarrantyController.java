package vn.pulsetech.product.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.pulsetech.product.domain.Warranty;
import vn.pulsetech.product.repository.WarrantyRepository;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/warranties")
public class WarrantyController {
    private final WarrantyRepository repository;

    public WarrantyController(WarrantyRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/search")
    public List<Warranty> searchWarranties(@RequestParam String query) {
        if (query == null || query.isBlank()) {
            return repository.findAll();
        }
        return repository.searchWarranties(query.trim());
    }

    @GetMapping
    public List<Warranty> getAllWarranties() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Warranty> getById(@PathVariable String id) {
        return repository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Warranty> createWarranty(@RequestBody Warranty warranty) {
        LocalDateTime start = warranty.startDate() != null ? warranty.startDate() : LocalDateTime.now();
        LocalDateTime end = warranty.endDate() != null ? warranty.endDate() : start.plusMonths(12);
        String status = warranty.status() != null ? warranty.status() : "ACTIVE";
        Warranty saved = repository.save(new Warranty(
                warranty.id(),
                warranty.imei() != null ? warranty.imei().trim() : "",
                warranty.orderId(),
                warranty.productId(),
                warranty.productName(),
                warranty.storageVariant(),
                warranty.customerName(),
                warranty.customerPhone(),
                warranty.customerEmail(),
                start,
                end,
                status,
                warranty.notes()
        ));
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Warranty> updateWarranty(@PathVariable String id, @RequestBody Warranty req) {
        return repository.findById(id).map(existing -> {
            Warranty updated = existing.withStatusAndNotes(
                    req.status() != null ? req.status() : existing.status(),
                    req.notes() != null ? req.notes() : existing.notes()
            );
            return ResponseEntity.ok(repository.save(updated));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWarranty(@PathVariable String id) {
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
