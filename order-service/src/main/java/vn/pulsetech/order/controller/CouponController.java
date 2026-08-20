package vn.pulsetech.order.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.pulsetech.order.domain.Coupon;
import vn.pulsetech.order.repository.CouponRepository;
import java.util.Optional;
import java.util.List;

@RestController
@RequestMapping("/api/orders/coupons")
public class CouponController {
    private final CouponRepository couponRepository;

    public CouponController(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateCoupon(@RequestParam String code) {
        Optional<Coupon> coupon = couponRepository.findByCode(code.toUpperCase());
        if (coupon.isPresent()) {
            Coupon c = coupon.get();
            if (c.isValid()) {
                return ResponseEntity.ok(c);
            } else {
                return ResponseEntity.badRequest().body("Coupon is expired or fully used.");
            }
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<List<Coupon>> getAllCoupons() {
        return ResponseEntity.ok(couponRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<Coupon> createCoupon(@RequestBody Coupon coupon) {
        // Ensure code is uppercase
        Coupon newCoupon = new Coupon(
            null, 
            coupon.code().toUpperCase(),
            coupon.description(),
            coupon.discountPercent(),
            coupon.discountAmount(),
            coupon.minOrderValue(),
            coupon.maxDiscountValue(),
            coupon.validFrom(),
            coupon.validUntil(),
            coupon.currentUsage(),
            coupon.maxUsage(),
            coupon.isActive()
        );
        return ResponseEntity.ok(couponRepository.save(newCoupon));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Coupon> updateCoupon(@PathVariable String id, @RequestBody Coupon coupon) {
        if (!couponRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        Coupon updatedCoupon = new Coupon(
            id, 
            coupon.code().toUpperCase(),
            coupon.description(),
            coupon.discountPercent(),
            coupon.discountAmount(),
            coupon.minOrderValue(),
            coupon.maxDiscountValue(),
            coupon.validFrom(),
            coupon.validUntil(),
            coupon.currentUsage(),
            coupon.maxUsage(),
            coupon.isActive()
        );
        return ResponseEntity.ok(couponRepository.save(updatedCoupon));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCoupon(@PathVariable String id) {
        if (!couponRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        couponRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
