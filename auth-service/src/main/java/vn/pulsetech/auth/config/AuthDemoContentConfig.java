package vn.pulsetech.auth.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.pulsetech.auth.domain.AppUser;
import vn.pulsetech.auth.domain.UserAddress;
import vn.pulsetech.auth.repository.UserAddressRepository;
import java.util.List;

@Configuration
public class AuthDemoContentConfig {
    @Bean
    public CommandLineRunner initAuthData(UserAddressRepository userAddressRepository,
                                           vn.pulsetech.auth.repository.AppUserRepository userRepository,
                                           org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        return args -> {
            if (userAddressRepository.count() == 0) {
                userAddressRepository.saveAll(List.of(
                    new UserAddress("ua-1", "user-1", "Nguyễn Văn A", "0912345678", "123 Đường Số 1", "Phường 1", "Quận 1", "TP. Hồ Chí Minh", true)
                ));
            }
            if (!userRepository.existsByEmailIgnoreCase("admin")) {
                AppUser admin = new AppUser("Quản Trị Viên", "admin", passwordEncoder.encode("admin"));
                admin.markVerified();
                admin.setRoles(java.util.Set.of("ADMIN", "USER"));
                userRepository.save(admin);
            }
            if (!userRepository.existsByEmailIgnoreCase("customer")) {
                AppUser customer = new AppUser("Khách Hàng Mẫu", "customer", passwordEncoder.encode("customer"));
                customer.markVerified();
                customer.setRoles(java.util.Set.of("USER"));
                userRepository.save(customer);
            }
        };
    }
}
