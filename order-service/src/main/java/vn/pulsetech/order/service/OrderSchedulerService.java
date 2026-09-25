package vn.pulsetech.order.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import vn.pulsetech.order.client.ProductClient;
import vn.pulsetech.order.domain.CustomerOrder;
import vn.pulsetech.order.domain.CustomerOrderItem;
import vn.pulsetech.order.repository.CustomerOrderRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class OrderSchedulerService {
    private final CustomerOrderRepository orderRepository;
    private final ProductClient productClient;

    public OrderSchedulerService(CustomerOrderRepository orderRepository, ProductClient productClient) {
        this.orderRepository = orderRepository;
        this.productClient = productClient;
    }

    // Run every 5 minutes to release unpaid online orders created more than 15 minutes ago
    @Scheduled(fixedRate = 300000)
    public void releaseUnpaidOrders() {
        LocalDateTime threshold = LocalDateTime.now(ZoneOffset.UTC).minusMinutes(15);
        List<CustomerOrder> allOrders = orderRepository.findAll();
        for (CustomerOrder order : allOrders) {
            if (order.getStatus() == 0 && order.getCreatedAt() != null && order.getCreatedAt().isBefore(threshold)) {
                // Check if it's an online payment method
                String method = order.getPaymentMethod() != null ? order.getPaymentMethod().toUpperCase() : "";
                if (!method.contains("COD") && !method.contains("NHẬN HÀNG")) {
                    order.setStatus(4); // CANCELLED
                    orderRepository.save(order);

                    // Auto-release stock back to product-service
                    for (CustomerOrderItem item : order.getItems()) {
                        productClient.increaseStock(item.getProductId(), item.getStorage(), item.getQty(), "UNPAID_CANCEL");
                    }
                }
            }
        }
    }
}
