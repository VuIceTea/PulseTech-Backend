package vn.pulsetech.order.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import vn.pulsetech.order.domain.CustomerOrder;

import java.util.Optional;

public interface CustomerOrderRepository extends MongoRepository<CustomerOrder, String> {
    Optional<CustomerOrder> findByIdAndCustomerPhone(String id, String customerPhone);
    java.util.List<CustomerOrder> findByCustomerEmailOrderByCreatedAtDesc(String customerEmail);
}