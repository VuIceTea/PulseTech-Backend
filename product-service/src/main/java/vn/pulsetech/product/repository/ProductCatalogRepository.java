package vn.pulsetech.product.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import vn.pulsetech.product.domain.Product;

public interface ProductCatalogRepository extends MongoRepository<Product, String> {}