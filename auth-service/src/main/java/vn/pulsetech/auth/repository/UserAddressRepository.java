package vn.pulsetech.auth.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.pulsetech.auth.domain.UserAddress;
import java.util.List;

@Repository
public interface UserAddressRepository extends MongoRepository<UserAddress, String> {
    List<UserAddress> findByUserId(String userId);
}
