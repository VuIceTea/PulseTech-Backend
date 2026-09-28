package vn.pulsetech.product.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.pulsetech.product.domain.Imei;
import vn.pulsetech.product.repository.ImeiRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products/imeis")
public class ImeiController {
    private final ImeiRepository repository;

    public ImeiController(ImeiRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Imei> getAllImeis(@RequestParam(required = false) String productId) {
        if (productId != null && !productId.isBlank()) {
            return repository.findByProductId(productId);
        }
        return repository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> addImei(@RequestBody Imei request) {
        Optional<Imei> existing = repository.findByImeiNumber(request.imeiNumber());
        if (existing.isPresent()) {
            return ResponseEntity.badRequest().body("IMEI đã tồn tại trong hệ thống!");
        }
        Imei saved = repository.save(new Imei(null, request.imeiNumber(), request.productId(), request.productName(), request.storageVariant(), "IN_STOCK", null, LocalDateTime.now()));
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteImei(@PathVariable String id) {
        repository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/sell")
    public ResponseEntity<?> sellImeis(@RequestBody java.util.Map<String, Object> body) {
        String orderId = (String) body.get("orderId");
        List<String> imeiNumbers = (List<String>) body.get("imeis");
        if (imeiNumbers == null || imeiNumbers.isEmpty()) return ResponseEntity.ok().build();
        for (String imeiNum : imeiNumbers) {
            repository.findByImeiNumber(imeiNum).ifPresent(imei -> {
                repository.save(new Imei(imei.id(), imei.imeiNumber(), imei.productId(), imei.productName(), imei.storageVariant(), "SOLD", orderId, imei.createdAt()));
            });
        }
        return ResponseEntity.ok().build();
    }
}
