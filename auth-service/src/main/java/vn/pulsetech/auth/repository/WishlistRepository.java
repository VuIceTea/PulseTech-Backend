package vn.pulsetech.auth.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.pulsetech.auth.domain.Wishlist;

@Repository
public interface WishlistRepository extends MongoRepository<Wishlist, String> {
}
