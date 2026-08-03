package vn.pulsetech.product.repository.content;

import org.springframework.data.mongodb.repository.MongoRepository;
import vn.pulsetech.product.domain.content.Navigation;
import java.util.List;

public interface NavigationRepository extends MongoRepository<Navigation, String> {
    List<Navigation> findAllByOrderByOrderAsc();
}
