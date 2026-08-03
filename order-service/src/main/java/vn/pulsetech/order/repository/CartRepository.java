package vn.pulsetech.order.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.pulsetech.order.domain.Cart;

@Repository
public interface CartRepository extends MongoRepository<Cart, String> {
}
