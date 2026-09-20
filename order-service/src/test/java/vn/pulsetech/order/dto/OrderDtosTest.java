package vn.pulsetech.order.dto;

import org.junit.jupiter.api.Test;
import vn.pulsetech.order.domain.CustomerOrder;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class OrderDtosTest {

    @Test
    void orderResponseShowsVietnamTimeFromStoredUtcTime() throws Exception {
        CustomerOrder order = new CustomerOrder("PT123456", "Nguyen Phi Vu", "vu@example.com",
                "0900000000", "Ho Chi Minh", "Thanh toán qua VNPay QR");
        setCreatedAt(order, LocalDateTime.of(2026, 9, 20, 5, 6));

        OrderDtos.OrderResponse response = OrderDtos.OrderResponse.from(order);

        assertThat(response.createdAt()).isEqualTo("20/09/2026 12:06");
    }

    private static void setCreatedAt(CustomerOrder order, LocalDateTime createdAt) throws Exception {
        Field field = CustomerOrder.class.getDeclaredField("createdAt");
        field.setAccessible(true);
        field.set(order, createdAt);
    }
}
