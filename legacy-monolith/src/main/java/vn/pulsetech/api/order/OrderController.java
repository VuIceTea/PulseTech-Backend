package vn.pulsetech.api.order;

import tools.jackson.databind.JsonNode;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import vn.pulsetech.api.product.ProductService;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final CustomerOrderRepository orders;
    private final ProductService products;

    public OrderController(CustomerOrderRepository orders, ProductService products) {
        this.orders = orders;
        this.products = products;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public OrderResponse create(@Valid @RequestBody CreateOrderRequest request) {
        String id;
        do { id = "PT" + ThreadLocalRandom.current().nextInt(100000, 1_000_000); } while (orders.existsById(id));
        CustomerOrder order = new CustomerOrder(id, request.customerName().trim(), request.customerPhone().trim(),
                request.address().trim(), paymentName(request.paymentMethod()));

        for (OrderItemRequest itemRequest : request.items()) {
            JsonNode product = products.findById(itemRequest.productId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sản phẩm không tồn tại: " + itemRequest.productId()));
            long price = Math.round(product.path("basePrice").asLong() * (1 - product.path("discount").asDouble() / 100));
            for (JsonNode storage : product.path("storages")) {
                if (itemRequest.storage().equals(storage.path("name").asText())) price += storage.path("priceOffset").asLong();
            }
            String image = product.path("image").asText();
            for (JsonNode color : product.path("colors")) {
                if (itemRequest.color().equals(color.path("name").asText())) image = color.path("image").asText(image);
            }
            order.addItem(new CustomerOrderItem(itemRequest.productId(), product.path("name").asText(), price,
                    itemRequest.quantity(), image, itemRequest.color(), itemRequest.storage()));
        }
        if ("PULSETECH".equalsIgnoreCase(request.couponCode()) || "CELLPHONES".equalsIgnoreCase(request.couponCode())) order.applyDiscount(10);
        if (order.getTotalPrice() <= 5_000_000) order.addShipping(30_000);
        return OrderResponse.from(orders.save(order));
    }

    @GetMapping("/track")
    public OrderResponse track(@RequestParam String orderId, @RequestParam String phone) {
        return orders.findByIdIgnoreCaseAndCustomerPhone(orderId.trim(), phone.trim())
                .map(OrderResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng"));
    }

    private String paymentName(String code) {
        return switch (code.toLowerCase(Locale.ROOT)) {
            case "vnpay" -> "Thanh toán qua VNPay QR";
            case "bank" -> "Chuyển khoản ngân hàng";
            default -> "Thanh toán khi nhận hàng (COD)";
        };
    }

    public record CreateOrderRequest(@NotBlank String customerName, @NotBlank String customerPhone,
                                     @NotBlank String address, @NotBlank String paymentMethod,
                                     String couponCode, @NotEmpty List<@Valid OrderItemRequest> items) {}
    public record OrderItemRequest(@NotBlank String productId, @NotBlank String color,
                                   @NotBlank String storage, @Min(1) int quantity) {}
    public record OrderItemResponse(Long id, String productId, String name, long price, int qty,
                                    String image, String color, String storage) {}
    public record OrderResponse(String id, int status, String customerName, String customerPhone, String address,
                                String paymentMethod, String createdAt, long totalPrice, List<OrderItemResponse> items) {
        static OrderResponse from(CustomerOrder order) {
            List<OrderItemResponse> items = order.getItems().stream().map(i -> new OrderItemResponse(
                    i.getId(), i.getProductId(), i.getName(), i.getPrice(), i.getQty(), i.getImage(), i.getColor(), i.getStorage())).toList();
            return new OrderResponse(order.getId(), order.getStatus(), order.getCustomerName(), order.getCustomerPhone(),
                    order.getAddress(), order.getPaymentMethod(), order.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.forLanguageTag("vi-VN"))),
                    order.getTotalPrice(), items);
        }
    }
}

