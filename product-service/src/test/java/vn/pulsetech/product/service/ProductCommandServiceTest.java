package vn.pulsetech.product.service;

import org.junit.jupiter.api.Test;
import vn.pulsetech.product.domain.Product;
import vn.pulsetech.product.repository.ProductCatalogRepository;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProductCommandServiceTest {
    @Test
    void missingVariantStockNeverInheritsParentStock() {
        assertThat(save(0, null, null).stock()).isZero();
        assertThat(save(25, 0, null).stock()).isZero();
        assertThat(save(25, null, 5).stock()).isEqualTo(5);
    }

    @Test
    void sumsIndependentVariantStock() {
        assertThat(save(25, 0, 0).stock()).isZero();
        assertThat(save(25, 0, 5).stock()).isEqualTo(5);
    }

    private Product save(int sharedStock, Integer firstStock, Integer secondStock) {
        ProductCatalogRepository repository = mock(ProductCatalogRepository.class);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        Product product = new Product("test", "Test", "Brand", "phone", 1, 1, 0, "",
                List.of(), List.of(), List.of(new Product.StorageVariant("256GB", 0, firstStock, null),
                new Product.StorageVariant("512GB", 0, secondStock, null)), null, "", "",
                0, 0, false, false, "", sharedStock);
        return new ProductCommandService(repository).save(product);
    }
}
