package vn.pulsetech.product.repository.content;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.pulsetech.product.domain.content.Article;

@Repository
public interface ArticleRepository extends MongoRepository<Article, String> {
}
