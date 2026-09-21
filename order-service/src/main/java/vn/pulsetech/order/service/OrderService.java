package vn.pulsetech.order.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import vn.pulsetech.order.client.ProductClient;
import vn.pulsetech.order.client.AuthClient;
import vn.pulsetech.order.client.ProductClient.ProductSnapshot;
import vn.pulsetech.order.domain.Coupon;
import vn.pulsetech.order.domain.CustomerOrder;
import vn.pulsetech.order.domain.CustomerOrderItem;
import vn.pulsetech.order.dto.OrderDtos.*;
import vn.pulsetech.order.repository.CouponRepository;
import vn.pulsetech.order.repository.CustomerOrderRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OrderService {
    private final CustomerOrderRepository orders;
    private final ProductClient products;
    private final PaymentService paymentService;
    private final CouponRepository coupons;
    private final AuthClient authClient;
    private final CartService cartService;

    public OrderService(CustomerOrderRepository orders, ProductClient products, PaymentService paymentService,
            CouponRepository coupons, AuthClient authClient, CartService cartService) {
        this.orders = orders;
        this.products = products;
        this.paymentService = paymentService;
        this.coupons = coupons;
        this.authClient = authClient;
        this.cartService = cartService;
    }

    public OrderResponse create(CreateOrderRequest request) {
        String paymentCode = request.paymentMethod().trim().toUpperCase(Locale.ROOT);
        if (!Set.of("COD", "VNPAY", "MOMO", "STRIPE").contains(paymentCode)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phương thức thanh toán không được hỗ trợ");
        }
        CustomerOrder order = new CustomerOrder(generateId(), request.customerName().trim(), request.customerEmail().trim(),
                request.customerPhone().trim(), request.address().trim(), paymentName(paymentCode));
        for (OrderItemRequest itemRequest : request.items()) {
            ProductSnapshot product = products.getRequiredProduct(itemRequest.productId());
            if (product.storages() == null || product.storages().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sản phẩm chưa có biến thể: " + product.name());
            }
            ProductSnapshot.StorageVariant storage = product.storages().stream()
                    .filter(value -> itemRequest.storage().equals(value.name()))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Biến thể không hợp lệ cho " + product.name() + ": " + itemRequest.storage()));
            int availableStock = storage.stock() == null ? 0 : storage.stock();
            if (availableStock < itemRequest.quantity()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        availableStock <= 0
                                ? "Biến thể " + itemRequest.storage() + " của " + product.name() + " đã hết hàng"
                                : "Biến thể " + itemRequest.storage() + " của " + product.name()
                                  + " chỉ còn " + availableStock + " sản phẩm");
            }
            if (product.colors() == null || product.colors().stream().noneMatch(c -> itemRequest.color().equals(c.name()))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Màu sắc không hợp lệ cho " + product.name() + ": " + itemRequest.color());
            }
            long price = product.basePrice() + storage.priceOffset();
            String image = product.colors().stream().filter(c -> itemRequest.color().equals(c.name()))
                    .map(ProductSnapshot.ColorVariant::image).findFirst().orElse(product.image());
            order.addItem(new CustomerOrderItem(product.id(), product.name(), price, itemRequest.quantity(),
                    image, itemRequest.color(), itemRequest.storage()));
        }
        long productSubtotal = order.getTotalPrice();
        long baseShippingFee = productSubtotal > 5_000_000 ? 0 : 30_000;

        Coupon productCoupon = null;
        Coupon shippingCoupon = null;
        List<String> acceptedCouponCodes = new ArrayList<>();
        for (String couponCode : requestedCouponCodes(request)) {
            List<Coupon> matchingCoupons = coupons.findAllByCode(couponCode);
            if (matchingCoupons.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã giảm giá không tồn tại");
            }
            Coupon coupon = matchingCoupons.stream()
                    .filter(value -> value.isValid() && productSubtotal >= value.minOrderValue())
                    .filter(value -> value.isAllowedFor(request.customerEmail()))
                    .min(Comparator.comparing(Coupon::validUntil))
                    .orElse(null);
            if (coupon == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã giảm giá không hợp lệ, đã hết lượt hoặc đơn hàng chưa đủ điều kiện");
            }
            if (isShippingCoupon(coupon)) {
                shippingCoupon = coupon;
            } else {
                productCoupon = coupon;
            }
            acceptedCouponCodes.add(coupon.code().trim().toUpperCase(Locale.ROOT));
        }
        order.setCouponCodes(new ArrayList<>(new LinkedHashSet<>(acceptedCouponCodes)));

        if (productCoupon != null) {
            order.applyFixedDiscount(calculateDiscount(productCoupon, productSubtotal));
        }

        long shippingDiscount = shippingCoupon == null ? 0 : calculateDiscount(shippingCoupon, baseShippingFee);
        long payableShippingFee = Math.max(0, baseShippingFee - shippingDiscount);
        if (payableShippingFee > 0) order.addShipping(payableShippingFee);

        String paymentUrl = null;
        if (!"COD".equals(paymentCode)) {
            paymentUrl = paymentService.createPaymentUrl(paymentCode, order.getId(), order.getTotalPrice());
        }

        order = orders.save(order);

        if ("COD".equals(paymentCode)) {
            decreaseStock(order);
            consumeCoupons(order);
            cartService.clearCart(order.getCustomerEmail());
        }
        
        int rewardPoints = (int) (order.getTotalPrice() / 1000);
        if (rewardPoints > 0) {
            authClient.addRewardPoints(order.getCustomerEmail(), rewardPoints);
        }

        return OrderResponse.from(order, paymentUrl);
    }

    public OrderResponse track(String orderId, String phone) {
        return orders.findByIdAndCustomerPhone(orderId.trim().toUpperCase(Locale.ROOT), phone.trim())
                .map(OrderResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng"));
    }

    public java.util.List<OrderResponse> getHistory(String email) {
        return orders.findByCustomerEmailOrderByCreatedAtDesc(email.trim().toLowerCase(Locale.ROOT)).stream()
                .map(order -> OrderResponse.from(order, null))
                .toList();
    }

    public java.util.List<OrderResponse> getAllOrders() {
        return orders.findAll().stream()
                .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                .map(order -> OrderResponse.from(order, null))
                .toList();
    }

    public void cancelOrder(String orderId) {
        CustomerOrder order = orders.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng"));
        if (order.getStatus() > 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không thể hủy đơn hàng ở trạng thái này");
        }
        order.setStatus(4); // 4 = Cancelled
        orders.save(order);
    }

    public void updateOrderStatus(String orderId, int status) {
        orders.findById(orderId).ifPresent(order -> {
            order.setStatus(status);
            orders.save(order);
        });
    }

    public void updateOrderPaymentInfo(String orderId, String transactionNo, String bankCode, String payDate) {
        orders.findById(orderId).ifPresent(order -> {
            order.setTransactionNo(transactionNo);
            order.setBankCode(bankCode);
            order.setPayDate(payDate);
            orders.save(order);
        });
    }

    public void completeOnlinePayment(String orderId, String transactionNo, String bankCode, String payDate) {
        orders.findById(orderId).ifPresent(order -> {
            if (order.getStatus() == 1) {
                return;
            }
            order.setTransactionNo(transactionNo);
            order.setBankCode(bankCode);
            order.setPayDate(payDate);
            order.setStatus(1);
            consumeCoupons(order);
            decreaseStock(order);
            orders.save(order);
            // The backend is the source of truth for payment completion. Clear
            // the user's cart here so it still works if the browser closes or
            // refreshes before the frontend callback finishes.
            cartService.clearCart(order.getCustomerEmail());
        });
    }

    private void decreaseStock(CustomerOrder order) {
        for (CustomerOrderItem item : order.getItems()) {
            products.decreaseStock(item.getProductId(), item.getStorage(), item.getQty());
        }
    }

    private void consumeCoupons(CustomerOrder order) {
        String email = order.getCustomerEmail();
        if (email == null || email.isBlank()) return;
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        for (String couponCode : order.getCouponCodes()) {
            if (couponCode == null || couponCode.isBlank()) continue;
            coupons.findAllByCode(couponCode.trim().toUpperCase(Locale.ROOT)).stream()
                    .filter(coupon -> coupon.remainingUsesFor(normalizedEmail) > 0)
                    .min(Comparator.comparing(Coupon::validUntil))
                    .ifPresent(coupon -> {
                List<String> assignedEmails = coupon.assignedEmails();
                if (assignedEmails == null || assignedEmails.isEmpty()) return;
                List<String> remainingEmails = new ArrayList<>(assignedEmails);
                for (int index = 0; index < remainingEmails.size(); index++) {
                    String assignedEmail = remainingEmails.get(index);
                    if (assignedEmail != null && normalizedEmail.equals(assignedEmail.trim().toLowerCase(Locale.ROOT))) {
                        remainingEmails.remove(index);
                        Coupon updated = new Coupon(coupon.id(), coupon.code(), coupon.description(),
                                coupon.discountPercent(), coupon.discountAmount(), coupon.minOrderValue(),
                                coupon.maxDiscountValue(), coupon.validFrom(), coupon.validUntil(),
                                coupon.currentUsage() + 1, coupon.maxUsage(), coupon.isActive(), remainingEmails);
                        coupons.save(updated);
                        return;
                    }
                }
            });
        }
    }

    private String generateId() {
        String id;
        do { id = "PT" + ThreadLocalRandom.current().nextInt(100000, 1_000_000); } while (orders.existsById(id));
        return id;
    }

    private String paymentName(String code) {
        return switch (code.toLowerCase(Locale.ROOT)) {
            case "vnpay" -> "Thanh toán qua VNPay QR";
            case "momo" -> "Thanh toán qua ví MoMo";
            case "stripe" -> "Thanh toán thẻ quốc tế qua Stripe";
            case "bank" -> "Chuyển khoản ngân hàng";
            default -> "Thanh toán khi nhận hàng (COD)";
        };
    }

    private List<String> requestedCouponCodes(CreateOrderRequest request) {
        List<String> codes = new ArrayList<>();
        if (request.couponCode() != null && !request.couponCode().isBlank()) {
            codes.add(request.couponCode().trim().toUpperCase(Locale.ROOT));
        }
        if (request.couponCodes() != null) {
            request.couponCodes().stream()
                    .filter(code -> code != null && !code.isBlank())
                    .map(code -> code.trim().toUpperCase(Locale.ROOT))
                    .forEach(codes::add);
        }
        return new ArrayList<>(new LinkedHashSet<>(codes));
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

    private boolean isShippingCoupon(Coupon coupon) {
        String code = coupon.code() == null ? "" : coupon.code().toUpperCase(Locale.ROOT);
        String description = coupon.description() == null ? "" : coupon.description().toUpperCase(Locale.ROOT);
        return code.contains("SHIP") || description.contains("VẬN CHUYỂN")
                || description.contains("VAN CHUYEN") || description.contains("GIAO HÀNG")
                || description.contains("GIAO HANG");
    }
}
