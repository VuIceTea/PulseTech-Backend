package vn.pulsetech.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import vn.pulsetech.order.domain.CustomerOrder;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public final class OrderDtos {
    private OrderDtos() {}

    public record CreateOrderRequest(@NotBlank String customerName, @NotBlank String customerEmail, @NotBlank String customerPhone,
            @NotBlank String address, @NotBlank String paymentMethod, String couponCode,
            @NotEmpty List<@Valid OrderItemRequest> items) {}
    public record OrderItemRequest(@NotBlank String productId, @NotBlank String color,
            @NotBlank String storage, @Min(1) int quantity) {}
    public record OrderItemResponse(String id, String productId, String name, long price, int qty,
            String image, String color, String storage) {}
    public record OrderResponse(String id, int status, String customerName, String customerEmail, String customerPhone,
            String address, String paymentMethod, String createdAt, long totalPrice,
            List<OrderItemResponse> items, String paymentUrl) {
        public static OrderResponse from(CustomerOrder order) {
            return from(order, null);
        }
        public static OrderResponse from(CustomerOrder order, String paymentUrl) {
            var items = order.getItems().stream().map(i -> new OrderItemResponse(i.getId(), i.getProductId(),
                    i.getName(), i.getPrice(), i.getQty(), i.getImage(), i.getColor(), i.getStorage())).toList();
            return new OrderResponse(order.getId(), order.getStatus(), order.getCustomerName(), order.getCustomerEmail(),
                    order.getCustomerPhone(), order.getAddress(), order.getPaymentMethod(),
                    order.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.forLanguageTag("vi-VN"))),
                    order.getTotalPrice(), items, paymentUrl);
        }
    }
}