package vn.pulsetech.product.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import vn.pulsetech.product.domain.FlashSale;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FlashSaleRepository extends MongoRepository<FlashSale, String> {
    List<FlashSale> findByIsActiveTrueAndStartTimeBeforeAndEndTimeAfter(LocalDateTime now1, LocalDateTime now2);
    Optional<FlashSale> findFirstByIsActiveTrueOrderByEndTimeAsc();
}
