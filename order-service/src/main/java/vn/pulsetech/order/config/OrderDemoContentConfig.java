package vn.pulsetech.order.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.pulsetech.order.domain.Coupon;
import vn.pulsetech.order.repository.CouponRepository;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class OrderDemoContentConfig {
    @Bean
    public CommandLineRunner initOrderData(CouponRepository couponRepository) {
        return args -> {
            if (couponRepository.count() == 0) {
                couponRepository.saveAll(List.of(
                    new Coupon("cp-1", "TET2026", "Giảm 10% dịp Tết Nguyên Đán", 10.0, 0, 500000, 2000000, LocalDateTime.now(), LocalDateTime.now().plusMonths(1), 0, 1000, true),
                    new Coupon("cp-2", "FREESHIP", "Miễn phí vận chuyển", 0, 50000, 0, 50000, LocalDateTime.now(), LocalDateTime.now().plusYears(1), 0, 5000, true)
                ));
            }
        };
    }
}
