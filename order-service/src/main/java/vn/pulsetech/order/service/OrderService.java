package vn.pulsetech.order.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import vn.pulsetech.order.client.ProductClient;
import vn.pulsetech.order.client.ProductClient.ProductSnapshot;
import vn.pulsetech.order.domain.CustomerOrder;
import vn.pulsetech.order.domain.CustomerOrderItem;
import vn.pulsetech.order.dto.OrderDtos.*;
import vn.pulsetech.order.repository.CustomerOrderRepository;

import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OrderService {
    private final CustomerOrderRepository orders;
    private final ProductClient products;
    private final PaymentService paymentService;

    public OrderService(CustomerOrderRepository orders, ProductClient products, PaymentService paymentService) {
        this.orders = orders;
        this.products = products;
        this.paymentService = paymentService;
    }

    public OrderResponse create(CreateOrderRequest request) {
        CustomerOrder order = new CustomerOrder(generateId(), request.customerName().trim(), request.customerEmail().trim(),
                request.customerPhone().trim(), request.address().trim(), paymentName(request.paymentMethod()));
        for (OrderItemRequest itemRequest : request.items()) {
            ProductSnapshot product = products.getRequiredProduct(itemRequest.productId());
            long price = Math.round(product.basePrice() * (1 - product.discount() / 100.0));
            if (product.storages() != null) {
                price += product.storages().stream().filter(s -> itemRequest.storage().equals(s.name()))
                        .mapToLong(ProductSnapshot.StorageVariant::priceOffset).findFirst().orElse(0);
            }
            String image = product.image();
            if (product.colors() != null) {
                image = product.colors().stream().filter(c -> itemRequest.color().equals(c.name()))
                        .map(ProductSnapshot.ColorVariant::image).findFirst().orElse(image);
            }
            order.addItem(new CustomerOrderItem(product.id(), product.name(), price, itemRequest.quantity(),
                    image, itemRequest.color(), itemRequest.storage()));
        }
        if ("PULSETECH".equalsIgnoreCase(request.couponCode()) || "CELLPHONES".equalsIgnoreCase(request.couponCode())) {
            order.applyDiscount(10);
        }
        if (order.getTotalPrice() <= 5_000_000) order.addShipping(30_000);
        order = orders.save(order);
        
        String paymentUrl = null;
        if ("VNPAY".equalsIgnoreCase(request.paymentMethod())) {
            paymentUrl = paymentService.createPaymentUrl(order.getId(), order.getTotalPrice());
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
            case "bank" -> "Chuyển khoản ngân hàng";
            default -> "Thanh toán khi nhận hàng (COD)";
        };
    }
}