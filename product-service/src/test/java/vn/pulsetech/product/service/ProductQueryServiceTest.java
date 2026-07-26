package vn.pulsetech.product.service;

import org.junit.jupiter.api.Test;
import vn.pulsetech.product.domain.Product;
import vn.pulsetech.product.repository.ProductCatalogRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProductQueryServiceTest {
    @Test
    void filtersCatalog() {
        ProductCatalogRepository repository = mock(ProductCatalogRepository.class);
        when(repository.findAll()).thenReturn(List.of(
                product("iphone-15", "iPhone 15", "Apple", "phone"),
                product("galaxy-s24", "Galaxy S24", "Samsung", "phone")));
        ProductQueryService service = new ProductQueryService(repository);

        assertThat(service.findAll("phone", "Apple", "iphone", null, null))
                .extracting(Product::id)
                .containsExactly("iphone-15");
    }

    private Product product(String id, String name, String brand, String category) {
        return new Product(id, name, brand, category, 1, 1, 0, "", List.of(), List.of(),
                List.of(), null, "", 0, 0, List.of(), false, false, "", 1);
    }
}