package vn.pulsetech.order.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.pulsetech.order.domain.Coupon;
import vn.pulsetech.order.repository.CouponRepository;
import java.util.Optional;

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
}
