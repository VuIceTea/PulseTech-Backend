package vn.pulsetech.product.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.pulsetech.product.domain.FlashSale;
import vn.pulsetech.product.repository.FlashSaleRepository;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/products/flash-sales")
public class FlashSaleController {
    private final FlashSaleRepository repository;

    public FlashSaleController(FlashSaleRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/active")
    public ResponseEntity<FlashSale> getActiveFlashSale() {
        LocalDateTime now = LocalDateTime.now();
        List<FlashSale> activeSales = repository.findByIsActiveTrueAndStartTimeBeforeAndEndTimeAfter(now, now);
        if (!activeSales.isEmpty()) {
            return ResponseEntity.ok(activeSales.get(0));
        }
        return repository.findFirstByIsActiveTrue().map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<FlashSale> getAllFlashSales() {
        return repository.findAll();
    }

    @PostMapping
    public FlashSale createFlashSale(@RequestBody FlashSale flashSale) {
        return repository.save(flashSale);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FlashSale> updateFlashSale(@PathVariable String id, @RequestBody FlashSale flashSale) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        FlashSale updated = new FlashSale(id, flashSale.title(), flashSale.startTime(), flashSale.endTime(), flashSale.isActive(), flashSale.items());
        return ResponseEntity.ok(repository.save(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFlashSale(@PathVariable String id) {
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
