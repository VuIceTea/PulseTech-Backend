package vn.pulsetech.product.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.pulsetech.product.domain.Product;
import vn.pulsetech.product.service.ProductCommandService;

@RestController
@RequestMapping("/internal/products")
public class InternalProductController {
    private final ProductCommandService commandService;

    public InternalProductController(ProductCommandService commandService) {
        this.commandService = commandService;
    }

    public record StockDecreaseRequest(String storage, int quantity) {}

    @PostMapping("/{id}/stock/decrease")
    public ResponseEntity<Product> decreaseStock(@PathVariable String id, @RequestBody StockDecreaseRequest request) {
        try {
            return ResponseEntity.ok(commandService.decrementStorageStock(id, request.storage(), request.quantity()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
