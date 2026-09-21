package vn.pulsetech.order.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.pulsetech.order.domain.Coupon;
import vn.pulsetech.order.repository.CouponRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders/coupons")
public class CouponController {
    private final CouponRepository couponRepository;

    public CouponController(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    public record ValidateCouponRequest(String code, long orderAmount, long shippingFee, List<String> productIds, String customerEmail) {}
    public record ValidateCouponResponse(boolean success, Object data) {}
    public record CouponDto(String code, long discountAmount, String discountType, String couponType, long finalAmount) {}
    public record CouponView(String id, String code, String description, double discountPercent, double discountAmount,
            long minOrderValue, long maxDiscountValue, LocalDateTime validFrom, LocalDateTime validUntil,
            int currentUsage, int maxUsage, boolean isActive, List<String> assignedEmails, int count, int usedCount) {}

    @PostMapping("/validate")
    public ResponseEntity<ValidateCouponResponse> validateCoupon(@RequestBody ValidateCouponRequest request) {
        if (request.code() == null || request.code().isBlank()) {
            return ResponseEntity.ok(new ValidateCouponResponse(false, "Vui lòng nhập mã giảm giá."));
        }
        List<Coupon> matchingCoupons = couponRepository.findAllByCode(normalizeCode(request.code()));
        if (matchingCoupons.isEmpty()) {
            return ResponseEntity.ok(new ValidateCouponResponse(false, "Mã giảm giá không tồn tại."));
        }

        Coupon c = matchingCoupons.stream()
                .filter(coupon -> coupon.isValid() && request.orderAmount() >= coupon.minOrderValue())
                .filter(coupon -> coupon.isAllowedFor(request.customerEmail()))
                .min(Comparator.comparing(Coupon::validUntil))
                .orElse(null);

        if (c == null) {
            boolean hasRemainingForCustomer = matchingCoupons.stream()
                    .anyMatch(coupon -> coupon.remainingUsesFor(request.customerEmail()) > 0);
            String message = hasRemainingForCustomer
                    ? "Mã giảm giá không hợp lệ, đã hết hạn hoặc đơn hàng chưa đủ điều kiện."
                    : "Mã giảm giá này không còn lượt sử dụng hoặc không áp dụng cho tài khoản của bạn.";
            return ResponseEntity.ok(new ValidateCouponResponse(false, message));
        }

        String couponType = couponType(c);
        long discountBase = "SHIPPING".equals(couponType) ? Math.max(0, request.shippingFee()) : request.orderAmount();
        String discountType = c.discountPercent() > 0 ? "PERCENTAGE" : "FIXED";
        long discountAmount = calculateDiscount(c, discountBase);
        long finalAmount = "SHIPPING".equals(couponType)
                ? request.orderAmount()
                : Math.max(0, request.orderAmount() - discountAmount);
        return ResponseEntity.ok(new ValidateCouponResponse(true,
                new CouponDto(c.code(), discountAmount, discountType, couponType, finalAmount)));
    }

    private long calculateDiscount(Coupon coupon, long baseAmount) {
        if (baseAmount <= 0) return 0;
        if (coupon.discountPercent() > 0) {
            long discount = Math.round(baseAmount * coupon.discountPercent() / 100.0);
            if (coupon.maxDiscountValue() > 0) {
                discount = Math.min(discount, coupon.maxDiscountValue());
            }
            return Math.min(discount, baseAmount);
        }
        if (coupon.discountAmount() > 0) {
            return Math.min(Math.round(coupon.discountAmount()), baseAmount);
        }
        return 0;
    }

    private String couponType(Coupon coupon) {
        String code = coupon.code() == null ? "" : coupon.code().toUpperCase();
        String description = coupon.description() == null ? "" : coupon.description().toUpperCase();
        if (code.contains("SHIP") || description.contains("VẬN CHUYỂN")
                || description.contains("VAN CHUYEN") || description.contains("GIAO HÀNG")
                || description.contains("GIAO HANG")) {
            return "SHIPPING";
        }
        return "PRODUCT";
    }

    @GetMapping
    public ResponseEntity<List<?>> getAllCoupons(@RequestParam(required = false) String email) {
        List<Coupon> all = couponRepository.findAll();
        if (email != null && !email.isBlank()) {
            Map<String, CouponView> grouped = new LinkedHashMap<>();
            all.stream()
                    .filter(coupon -> coupon.isActive()
                            && LocalDateTime.now().isAfter(coupon.validFrom())
                            && LocalDateTime.now().isBefore(coupon.validUntil()))
                    .filter(coupon -> coupon.isVisibleFor(email))
                    .sorted(Comparator.comparing(Coupon::validUntil))
                    .forEach(coupon -> {
                        String key = normalizeCode(coupon.code());
                        CouponView current = grouped.get(key);
                        CouponView next = toView(coupon, email);
                        if (current == null) {
                            grouped.put(key, next);
                        } else {
                            grouped.put(key, new CouponView(current.id(), current.code(), current.description(),
                                    current.discountPercent(), current.discountAmount(), current.minOrderValue(),
                                    current.maxDiscountValue(), current.validFrom(), current.validUntil(),
                                    current.currentUsage(), current.maxUsage(), current.isActive(),
                                    current.assignedEmails(), current.count() + next.count(),
                                    current.usedCount() + next.usedCount()));
                        }
                    });
            return ResponseEntity.ok(grouped.values().stream().toList());
        }
        Map<String, CouponView> grouped = new LinkedHashMap<>();
        all.stream()
                .sorted(Comparator.comparing(Coupon::validUntil))
                .forEach(coupon -> {
                    String key = normalizeCode(coupon.code());
                    CouponView next = toAdminView(coupon);
                    CouponView current = grouped.get(key);
                    if (current == null) {
                        grouped.put(key, next);
                    } else {
                        List<String> mergedEmails = new ArrayList<>();
                        if (current.assignedEmails() != null) mergedEmails.addAll(current.assignedEmails());
                        if (next.assignedEmails() != null) mergedEmails.addAll(next.assignedEmails());
                    grouped.put(key, new CouponView(current.id(), current.code(), current.description(),
                            current.discountPercent(), current.discountAmount(), current.minOrderValue(),
                            current.maxDiscountValue(), current.validFrom(), current.validUntil(),
                            current.currentUsage() + next.currentUsage(), current.maxUsage() + next.maxUsage(),
                            current.isActive() || next.isActive(), mergedEmails,
                            maxVoucherCountPerCustomer(mergedEmails),
                            current.usedCount() + next.usedCount()));
                    }
                });
        return ResponseEntity.ok(grouped.values().stream().toList());
    }

    @PostMapping
    public ResponseEntity<Coupon> createCoupon(@RequestBody Coupon coupon) {
        String id = coupon.id() != null && !coupon.id().isEmpty() ? coupon.id() : UUID.randomUUID().toString();
        Coupon newCoupon = new Coupon(
                id,
                normalizeCode(coupon.code()),
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
                normalizeEmails(coupon.assignedEmails()),
                normalizeEmails(coupon.usedEmails())
        );
        return ResponseEntity.ok(couponRepository.save(newCoupon));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Coupon> updateCoupon(@PathVariable String id, @RequestBody Coupon coupon) {
        Coupon existingCoupon = couponRepository.findById(id).orElse(null);
        if (existingCoupon == null) {
            return ResponseEntity.notFound().build();
        }

        List<String> assignedEmails = new ArrayList<>();
        List<String> existingEmails = normalizeEmails(existingCoupon.assignedEmails());
        List<String> publishedEmails = uniqueEmails(normalizeEmails(coupon.assignedEmails()));
        if (existingEmails != null) assignedEmails.addAll(existingEmails);
        if (publishedEmails != null) assignedEmails.addAll(publishedEmails);

        Coupon updatedCoupon = new Coupon(
                id,
                normalizeCode(coupon.code()),
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
                assignedEmails,
                normalizeEmails(existingCoupon.usedEmails())
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

    private CouponView toView(Coupon coupon, String email) {
        return new CouponView(coupon.id(), coupon.code(), coupon.description(), coupon.discountPercent(),
                coupon.discountAmount(), coupon.minOrderValue(), coupon.maxDiscountValue(), coupon.validFrom(),
                coupon.validUntil(), coupon.currentUsage(), coupon.maxUsage(), coupon.isActive(),
                coupon.assignedEmails(), coupon.remainingUsesFor(email), coupon.usedCountFor(email));
    }

    private CouponView toAdminView(Coupon coupon) {
        return new CouponView(coupon.id(), coupon.code(), coupon.description(), coupon.discountPercent(),
                coupon.discountAmount(), coupon.minOrderValue(), coupon.maxDiscountValue(), coupon.validFrom(),
                coupon.validUntil(), coupon.currentUsage(), coupon.maxUsage(), coupon.isActive(),
                coupon.assignedEmails(), maxVoucherCountPerCustomer(coupon.assignedEmails()),
                coupon.usedEmails() == null ? 0 : coupon.usedEmails().size());
    }

    private int maxVoucherCountPerCustomer(List<String> assignedEmails) {
        if (assignedEmails == null || assignedEmails.isEmpty()) {
            return 0;
        }

        Map<String, Long> counts = assignedEmails.stream()
                .filter(Objects::nonNull)
                .map(email -> email.trim().toLowerCase())
                .filter(email -> !email.isBlank())
                .collect(Collectors.groupingBy(email -> email, LinkedHashMap::new, Collectors.counting()));

        return counts.values().stream().mapToInt(Long::intValue).max().orElse(0);
    }

    private List<String> normalizeEmails(List<String> emails) {
        if (emails == null) return null;
        List<String> normalized = new ArrayList<>();
        for (String email : emails) {
            if (email != null && !email.isBlank()) {
                normalized.add(email.trim().toLowerCase(Locale.ROOT));
            }
        }
        return normalized;
    }

    private List<String> uniqueEmails(List<String> emails) {
        if (emails == null) return null;
        return emails.stream().distinct().toList();
    }

    private String normalizeCode(String code) {
        return code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
    }
}
