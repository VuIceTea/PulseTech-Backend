package vn.pulsetech.product.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import vn.pulsetech.product.domain.Warranty;

import java.util.List;
import java.util.Optional;

public interface WarrantyRepository extends MongoRepository<Warranty, String> {
    Optional<Warranty> findByImei(String imei);
    List<Warranty> findByCustomerPhone(String customerPhone);
    List<Warranty> findByCustomerEmail(String customerEmail);
    List<Warranty> findByOrderId(String orderId);

    @Query("{ '$or': [ { 'imei': { '$regex': ?0, '$options': 'i' } }, { 'customerPhone': { '$regex': ?0, '$options': 'i' } }, { 'orderId': { '$regex': ?0, '$options': 'i' } }, { 'customerEmail': { '$regex': ?0, '$options': 'i' } } ] }")
    List<Warranty> searchWarranties(String query);
}
