package vn.pulsetech.api.order;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, String> {
    Optional<CustomerOrder> findByIdIgnoreCaseAndCustomerPhone(String id, String customerPhone);
}
