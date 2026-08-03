package vn.pulsetech.product.repository.content;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.pulsetech.product.domain.content.FooterLink;

@Repository
public interface FooterLinkRepository extends MongoRepository<FooterLink, String> {
}
