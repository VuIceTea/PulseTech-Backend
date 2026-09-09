package vn.pulsetech.product.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationRunner;
import tools.jackson.databind.ObjectMapper;
import vn.pulsetech.product.domain.Product;
import vn.pulsetech.product.repository.ProductCatalogRepository;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DemoDataConfigTest {
    @Test
    void backfillsMissingContentWithoutOverwritingProductData() throws Exception {
        ProductCatalogRepository repository = mock(ProductCatalogRepository.class);
        Product existing = product("iphone-15-pro-max", null);
        when(repository.findById("iphone-15-pro-max")).thenReturn(Optional.of(existing));
        when(repository.findById(org.mockito.ArgumentMatchers.argThat(id -> !"iphone-15-pro-max".equals(id))))
                .thenReturn(Optional.of(product("existing-product", "Nội dung đã chỉnh")));

        ApplicationRunner runner = new DemoDataConfig().seedProductData(repository, new ObjectMapper());
        runner.run(null);

        verify(repository).save(org.mockito.ArgumentMatchers.argThat(saved ->
                "iphone-15-pro-max".equals(saved.id())
                        && saved.content() != null
                        && saved.content().contains("iPhone 15 Pro Max")
                        && saved.stock() == existing.stock()));
    }

    @Test
    void keepsExistingArticleUntouched() throws Exception {
        ProductCatalogRepository repository = mock(ProductCatalogRepository.class);
        when(repository.findById(any())).thenReturn(Optional.of(product("existing-product", "<p>Nội dung đã chỉnh</p>")));

        ApplicationRunner runner = new DemoDataConfig().seedProductData(repository, new ObjectMapper());
        runner.run(null);

        verify(repository, never()).save(any());
    }

    private Product product(String id, String content) {
        return new Product(id, "iPhone 15 Pro Max", "Apple", "phone", 1, 1, 0, "", List.of(),
                List.of(), List.of(), new Product.ProductSpec("OLED", "iOS", "48 MP", "12 MP", "A17 Pro",
                "8 GB", "256 GB", "4441 mAh", null, null, null, null, null, null, null, null, null),
                "Mô tả", content, 0, 0, false, false, "", 25);
    }
}
