package vn.pulsetech.product.repository.content;

import org.springframework.data.mongodb.repository.MongoRepository;
import vn.pulsetech.product.domain.content.Category;
import java.util.List;

public interface CategoryRepository extends MongoRepository<Category, String> {
    List<Category> findAllByOrderByOrderAsc();
}
