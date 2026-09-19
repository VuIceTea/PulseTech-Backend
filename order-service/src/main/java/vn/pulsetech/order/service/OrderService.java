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
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OrderService {
    private final CustomerOrderRepository orders;
    private final ProductClient products;
    private final PaymentService paymentService;
    private final CouponRepository coupons;
    private final AuthClient authClient;

    public OrderService(CustomerOrderRepository orders, ProductClient products, PaymentService paymentService, CouponRepository coupons, AuthClient authClient) {
        this.orders = orders;
        this.products = products;
        this.paymentService = paymentService;
        this.coupons = coupons;
        this.authClient = authClient;
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
        // Apply coupon discount from database
        if (request.couponCode() != null && !request.couponCode().isBlank()) {
            Optional<Coupon> couponOpt = coupons.findByCode(request.couponCode().trim().toUpperCase());
            Coupon coupon = couponOpt.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã giảm giá không tồn tại"));
            if (!coupon.isAllowedFor(request.customerEmail()) || !coupon.isValid() || order.getTotalPrice() < coupon.minOrderValue()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã giảm giá không hợp lệ hoặc đơn hàng chưa đủ điều kiện");
            }
            if (coupon.discountPercent() > 0) {
                long discount = Math.round(order.getTotalPrice() * coupon.discountPercent() / 100.0);
                if (coupon.maxDiscountValue() > 0) {
                    discount = Math.min(discount, coupon.maxDiscountValue());
                }
                order.applyFixedDiscount(discount);
            } else if (coupon.discountAmount() > 0) {
                order.applyFixedDiscount(Math.round(coupon.discountAmount()));
            }
        }
        if (order.getTotalPrice() <= 5_000_000) order.addShipping(30_000);

        String paymentUrl = null;
        if (!"COD".equals(paymentCode)) {
            paymentUrl = paymentService.createPaymentUrl(paymentCode, order.getId(), order.getTotalPrice());
        }

        order = orders.save(order);
        
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
}
