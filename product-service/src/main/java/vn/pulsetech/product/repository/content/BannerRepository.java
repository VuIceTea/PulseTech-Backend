package vn.pulsetech.product.repository.content;

import org.springframework.data.mongodb.repository.MongoRepository;
import vn.pulsetech.product.domain.content.Banner;
import java.util.List;

public interface BannerRepository extends MongoRepository<Banner, String> {
    List<Banner> findAllByOrderByOrderAsc();
}
