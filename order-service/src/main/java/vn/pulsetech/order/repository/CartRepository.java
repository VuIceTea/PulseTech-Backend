package vn.pulsetech.order.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import vn.pulsetech.order.domain.Cart;

@Repository
public interface CartRepository extends CrudRepository<Cart, String> {
}
