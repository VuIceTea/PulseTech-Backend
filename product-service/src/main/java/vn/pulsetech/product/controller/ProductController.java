package vn.pulsetech.product.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.pulsetech.product.domain.Product;
import vn.pulsetech.product.service.ProductQueryService;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductQueryService service;
    public ProductController(ProductQueryService service) { this.service = service; }

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
}
