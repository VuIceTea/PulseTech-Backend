package vn.pulsetech.order.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import vn.pulsetech.order.client.ProductClient;
import vn.pulsetech.order.domain.CustomerOrder;
import vn.pulsetech.order.domain.CustomerOrderItem;
import vn.pulsetech.order.domain.ReturnRequest;
import vn.pulsetech.order.repository.CustomerOrderRepository;
import vn.pulsetech.order.repository.ReturnRequestRepository;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders/returns")
public class ReturnRequestController {
    private final ReturnRequestRepository repository;
    private final CustomerOrderRepository orderRepository;
    private final ProductClient productClient;

    public ReturnRequestController(ReturnRequestRepository repository, CustomerOrderRepository orderRepository, ProductClient productClient) {
        this.repository = repository;
        this.orderRepository = orderRepository;
        this.productClient = productClient;
    }

    @PostMapping
    public ResponseEntity<ReturnRequest> createReturnRequest(@RequestBody ReturnRequest req) {
        CustomerOrder order = orderRepository.findById(req.orderId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng"));
        
        ReturnRequest saved = repository.save(new ReturnRequest(
                null,
                order.getId(),
                order.getCustomerEmail(),
                order.getCustomerName(),
                order.getCustomerPhone(),
                req.reason(),
                req.description(),
                req.imageUrl(),
                "PENDING",
                order.getTotalPrice(),
                LocalDateTime.now()
        ));
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public List<ReturnRequest> getAllReturns() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    @GetMapping("/my")
    public List<ReturnRequest> getMyReturns(@RequestParam String email) {
        return repository.findByCustomerEmailOrderByCreatedAtDesc(email);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ReturnRequest> updateStatus(@PathVariable String id, @RequestParam String status) {
        return repository.findById(id).map(returnReq -> {
            ReturnRequest updated = returnReq.withStatus(status);
            ReturnRequest saved = repository.save(updated);

            // Auto-restock if approved
            if ("APPROVED".equalsIgnoreCase(status) || "RESTOCKED".equalsIgnoreCase(status)) {
                orderRepository.findById(saved.orderId()).ifPresent(order -> {
                    for (CustomerOrderItem item : order.getItems()) {
                        productClient.increaseStock(item.getProductId(), item.getStorage(), item.getQty(), "RETURN_RESTOCK");
                    }
                });
            }
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }
}
