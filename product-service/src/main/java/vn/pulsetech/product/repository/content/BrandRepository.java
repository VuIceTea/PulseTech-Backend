package vn.pulsetech.product.repository.content;

import org.springframework.data.mongodb.repository.MongoRepository;
import vn.pulsetech.product.domain.content.Brand;
import java.util.List;

public interface BrandRepository extends MongoRepository<Brand, String> {
    List<Brand> findAllByOrderByOrderAsc();
}
