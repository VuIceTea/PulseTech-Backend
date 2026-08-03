package vn.pulsetech.product.repository.content;

import org.springframework.data.mongodb.repository.MongoRepository;
import vn.pulsetech.product.domain.content.Policy;

public interface PolicyRepository extends MongoRepository<Policy, String> {
}
