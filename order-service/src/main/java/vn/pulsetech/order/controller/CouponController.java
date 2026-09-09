package vn.pulsetech.order.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.pulsetech.order.domain.Coupon;
import vn.pulsetech.order.repository.CouponRepository;
import java.util.Optional;
import java.util.List;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders/coupons")
public class CouponController {
    private final CouponRepository couponRepository;

    public CouponController(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    public record ValidateCouponRequest(String code, long orderAmount, List<String> productIds, String customerEmail) {}
    public record ValidateCouponResponse(boolean success, Object data) {}
    public record CouponDto(String code, long discountAmount, String discountType, long finalAmount) {}

    @PostMapping("/validate")
    public ResponseEntity<ValidateCouponResponse> validateCoupon(@RequestBody ValidateCouponRequest request) {
        if (request.code() == null || request.code().isBlank()) {
            return ResponseEntity.ok(new ValidateCouponResponse(false, "Vui lòng nhập mã giảm giá."));
        }
        Optional<Coupon> couponOpt = couponRepository.findByCode(request.code().toUpperCase().trim());
        if (couponOpt.isPresent()) {
            Coupon c = couponOpt.get();
            if (!c.isAllowedFor(request.customerEmail())) {
                return ResponseEntity.ok(new ValidateCouponResponse(false, "Mã giảm giá này không áp dụng cho tài khoản của bạn."));
            }
            if (c.isValid() && request.orderAmount() >= c.minOrderValue()) {
                long discountAmount = 0;
                String discountType = "FIXED";
                if (c.discountPercent() > 0) {
                    discountType = "PERCENTAGE";
                    discountAmount = Math.round(request.orderAmount() * c.discountPercent() / 100.0);
                    if (c.maxDiscountValue() > 0) {
                        discountAmount = Math.min(discountAmount, c.maxDiscountValue());
                    }
                } else if (c.discountAmount() > 0) {
                    discountAmount = Math.round(c.discountAmount());
                }
                long finalAmount = Math.max(0, request.orderAmount() - discountAmount);
                return ResponseEntity.ok(new ValidateCouponResponse(true, new CouponDto(c.code(), discountAmount, discountType, finalAmount)));
            } else {
                return ResponseEntity.ok(new ValidateCouponResponse(false, "Mã giảm giá không hợp lệ, đã hết hạn hoặc đơn hàng chưa đủ điều kiện."));
            }
        }
        return ResponseEntity.ok(new ValidateCouponResponse(false, "Mã giảm giá không tồn tại."));
    }

    @GetMapping
    public ResponseEntity<List<Coupon>> getAllCoupons(@RequestParam(required = false) String email) {
        List<Coupon> all = couponRepository.findAll();
        if (email != null && !email.isBlank()) {
            return ResponseEntity.ok(all.stream().filter(c -> c.isAllowedFor(email)).toList());
        }
        return ResponseEntity.ok(all);
    }

    @PostMapping
    public ResponseEntity<Coupon> createCoupon(@RequestBody Coupon coupon) {
        String id = coupon.id() != null && !coupon.id().isEmpty() ? coupon.id() : UUID.randomUUID().toString();
        Coupon newCoupon = new Coupon(
                id,
                coupon.code().toUpperCase(),
                coupon.description(),
                coupon.discountPercent(),
                coupon.discountAmount(),
                coupon.minOrderValue(),
                coupon.maxDiscountValue(),
                coupon.validFrom() != null ? coupon.validFrom() : LocalDateTime.now(),
                coupon.validUntil() != null ? coupon.validUntil() : LocalDateTime.now().plusMonths(1),
                coupon.currentUsage(),
                coupon.maxUsage() > 0 ? coupon.maxUsage() : 100,
                coupon.isActive(),
                coupon.assignedEmails()
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
                coupon.isActive(),
                coupon.assignedEmails()
        );
        return ResponseEntity.ok(couponRepository.save(updatedCoupon));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCoupon(@PathVariable String id) {
        if (couponRepository.existsById(id)) {
            couponRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
