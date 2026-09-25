package vn.pulsetech.order.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import vn.pulsetech.order.domain.ReturnRequest;

import java.util.List;

public interface ReturnRequestRepository extends MongoRepository<ReturnRequest, String> {
    List<ReturnRequest> findByCustomerEmailOrderByCreatedAtDesc(String customerEmail);
    List<ReturnRequest> findAllByOrderByCreatedAtDesc();
}
