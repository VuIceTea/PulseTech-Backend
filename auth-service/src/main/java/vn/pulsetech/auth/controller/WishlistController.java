package vn.pulsetech.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.pulsetech.auth.domain.Wishlist;
import vn.pulsetech.auth.repository.WishlistRepository;
import java.util.Optional;

@RestController
@RequestMapping("/api/users/wishlist")
public class WishlistController {
    private final WishlistRepository wishlistRepository;

    public WishlistController(WishlistRepository wishlistRepository) {
        this.wishlistRepository = wishlistRepository;
    }

    @GetMapping
    public ResponseEntity<Wishlist> getWishlist(@RequestParam String userId) {
        Optional<Wishlist> wishlist = wishlistRepository.findById(userId);
        return wishlist.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.ok(new Wishlist(userId)));
    }

    @PostMapping
    public ResponseEntity<Wishlist> saveWishlist(@RequestBody Wishlist wishlist) {
        return ResponseEntity.ok(wishlistRepository.save(wishlist));
    }
}
