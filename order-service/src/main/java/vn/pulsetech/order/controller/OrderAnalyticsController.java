package vn.pulsetech.order.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.pulsetech.order.domain.CustomerOrder;
import vn.pulsetech.order.repository.CustomerOrderRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders/analytics")
public class OrderAnalyticsController {
    private final CustomerOrderRepository orderRepository;

    public OrderAnalyticsController(CustomerOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @GetMapping("/profit")
    public Map<String, Object> getProfitAnalytics() {
        List<CustomerOrder> orders = orderRepository.findAll();
        long totalRevenue = 0;
        long estimatedTotalCost = 0;
        int completedOrders = 0;
        int pendingOrders = 0;
        int processingOrders = 0;
        int shippingOrders = 0;
        int cancelledOrders = 0;

        for (CustomerOrder order : orders) {
            int status = order.getStatus();
            if (status == 0) pendingOrders++;
            else if (status == 1) processingOrders++;
            else if (status == 2) shippingOrders++;
            else if (status == 3) {
                completedOrders++;
                totalRevenue += order.getTotalPrice();
                // Estimate cost as ~70% of revenue if not specified
                estimatedTotalCost += Math.round(order.getTotalPrice() * 0.70);
            } else if (status == 4) cancelledOrders++;
        }

        long netProfit = Math.max(0, totalRevenue - estimatedTotalCost);

        Map<String, Object> result = new HashMap<>();
        result.put("totalOrders", orders.size());
        result.put("totalRevenue", totalRevenue);
        result.put("estimatedTotalCost", estimatedTotalCost);
        result.put("netProfit", netProfit);
        result.put("completedOrders", completedOrders);
        result.put("pendingOrders", pendingOrders);
        result.put("processingOrders", processingOrders);
        result.put("shippingOrders", shippingOrders);
        result.put("cancelledOrders", cancelledOrders);

        return result;
    }
}
