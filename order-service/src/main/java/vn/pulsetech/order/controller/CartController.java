package vn.pulsetech.order.controller;

import org.springframework.web.bind.annotation.*;
import vn.pulsetech.order.domain.Cart;
import vn.pulsetech.order.domain.CartItem;
import vn.pulsetech.order.service.CartService;

import java.util.Map;

@RestController
@RequestMapping("/api/orders/cart")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public Cart getCart(@RequestParam String userId) {
        return cartService.getCart(userId);
    }

    @PostMapping("/items")
    public Cart updateCartItem(@RequestParam String userId, @RequestBody CartItem item) {
        return cartService.updateCart(userId, item);
    }

    @DeleteMapping("/items/{productId}")
    public Cart removeCartItem(@RequestParam String userId, @PathVariable String productId, 
                               @RequestParam String color, @RequestParam String storage) {
        return cartService.removeItem(userId, productId, color, storage);
    }

    @DeleteMapping
    public Cart clearCart(@RequestParam String userId) {
        return cartService.clearCart(userId);
    }

    @PostMapping("/merge")
    public Cart mergeCarts(@RequestBody Map<String, String> payload) {
        return cartService.mergeCarts(payload.get("guestId"), payload.get("userId"));
    }
}
