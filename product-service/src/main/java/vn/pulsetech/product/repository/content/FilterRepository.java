package vn.pulsetech.product.repository.content;

import org.springframework.data.mongodb.repository.MongoRepository;
import vn.pulsetech.product.domain.content.Filter;

public interface FilterRepository extends MongoRepository<Filter, String> {
}
