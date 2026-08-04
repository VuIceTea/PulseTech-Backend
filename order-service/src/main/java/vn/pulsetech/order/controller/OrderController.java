package vn.pulsetech.order.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vn.pulsetech.order.dto.OrderDtos.CreateOrderRequest;
import vn.pulsetech.order.dto.OrderDtos.OrderResponse;
import vn.pulsetech.order.service.OrderService;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService service;
    public OrderController(OrderService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@Valid @RequestBody CreateOrderRequest request) { return service.create(request); }

    @GetMapping("/track")
    public OrderResponse track(@RequestParam String orderId, @RequestParam String phone) {
        return service.track(orderId, phone);
    }

    @GetMapping("/history")
    public java.util.List<OrderResponse> getHistory(@RequestParam String email) {
        return service.getHistory(email);
    }

    @PostMapping("/cancel")
    public void cancelOrder(@RequestParam String orderId) {
        service.cancelOrder(orderId);
    }

    @GetMapping("/all")
    public java.util.List<OrderResponse> getAllOrders() {
        return service.getAllOrders();
    }

    @PatchMapping("/{id}/status")
    public void updateOrderStatus(@PathVariable String id, @RequestParam int status) {
        service.updateOrderStatus(id, status);
    }
}
