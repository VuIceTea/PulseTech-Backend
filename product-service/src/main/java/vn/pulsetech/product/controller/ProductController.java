package vn.pulsetech.product.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.pulsetech.product.domain.Product;
import vn.pulsetech.product.dto.UpdateDiscountRequest;
import vn.pulsetech.product.service.ProductCommandService;
import vn.pulsetech.product.service.ProductQueryService;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductQueryService service;
    private final ProductCommandService commandService;

    public ProductController(ProductQueryService service, ProductCommandService commandService) { 
        this.service = service; 
        this.commandService = commandService;
    }

    @GetMapping
    public List<Product> getProducts(@RequestParam(required = false) String category,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(required = false) Boolean flashSale) {
        return service.findAll(category, brand, search, featured, flashSale);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable String id) {
        return service.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/discount")
    public ResponseEntity<Product> updateDiscount(@PathVariable String id, @RequestBody UpdateDiscountRequest request) {
        try {
            return ResponseEntity.ok(commandService.updateDiscount(id, request.discount()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        return ResponseEntity.ok(commandService.save(product));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable String id, @RequestBody Product product) {
        return ResponseEntity.ok(commandService.save(product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        commandService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
