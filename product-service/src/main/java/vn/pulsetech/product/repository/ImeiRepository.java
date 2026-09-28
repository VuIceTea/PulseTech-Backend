package vn.pulsetech.product.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import vn.pulsetech.product.domain.Imei;

import java.util.List;
import java.util.Optional;

public interface ImeiRepository extends MongoRepository<Imei, String> {
    List<Imei> findByProductId(String productId);
    List<Imei> findByProductIdAndStorageVariant(String productId, String storageVariant);
    Optional<Imei> findByImeiNumber(String imeiNumber);
}
