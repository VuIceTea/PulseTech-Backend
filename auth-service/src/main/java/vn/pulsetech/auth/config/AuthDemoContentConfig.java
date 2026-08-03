package vn.pulsetech.auth.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.pulsetech.auth.domain.UserAddress;
import vn.pulsetech.auth.repository.UserAddressRepository;
import java.util.List;

@Configuration
public class AuthDemoContentConfig {
    @Bean
    public CommandLineRunner initAuthData(UserAddressRepository userAddressRepository) {
        return args -> {
            if (userAddressRepository.count() == 0) {
                userAddressRepository.saveAll(List.of(
                    new UserAddress("ua-1", "user-1", "Nguyễn Văn A", "0912345678", "123 Đường Số 1", "Phường 1", "Quận 1", "TP. Hồ Chí Minh", true)
                ));
            }
        };
    }
}
