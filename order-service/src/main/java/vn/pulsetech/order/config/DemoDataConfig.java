package vn.pulsetech.order.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.pulsetech.order.domain.CustomerOrder;
import vn.pulsetech.order.domain.CustomerOrderItem;
import vn.pulsetech.order.repository.CustomerOrderRepository;

@Configuration
public class DemoDataConfig {
    @Bean
    ApplicationRunner demoOrder(CustomerOrderRepository orders) {
        return args -> {
            if (orders.existsById("PT123456")) return;
            CustomerOrder order = new CustomerOrder("PT123456", "Nguyễn Văn A", "nguyenvana@example.com", "0987654321",
                    "123 Đường Nguyễn Huệ, Phường Bến Nghé, Quận 1, TP. Hồ Chí Minh", "Thanh toán khi nhận hàng (COD)");
            order.setStatus(2);
            order.addItem(new CustomerOrderItem("iphone-15-pro-max", "iPhone 15 Pro Max 256GB - Titan Tự Nhiên",
                    21_990_000, 1, "/hot-sale/iphone-15-pro-max.png", "Titan Tự Nhiên", "256GB"));
            order.addItem(new CustomerOrderItem("op-lung-magsafe-iphone-15", "Ốp lưng MagSafe trong suốt",
                    500_000, 1, "/accessories/op-lung-iphone-15-pro-trong-suot-magsafe.png", "Trong suốt", "Tiêu chuẩn"));
            orders.save(order);
        };
    }
}
